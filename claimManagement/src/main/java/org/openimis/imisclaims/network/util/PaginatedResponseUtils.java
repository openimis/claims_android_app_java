package org.openimis.imisclaims.network.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import org.openimis.imisclaims.network.exception.HttpException;
import org.openimis.imisclaims.network.request.BaseFHIRGetPaginatedRequest;
import org.openimis.imisclaims.network.response.PaginatedResponse;
import org.openimis.imisclaims.tools.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class PaginatedResponseUtils {
    private static final String LOG_TAG = "FHIR_SYNC_TOLERANCE";

    private PaginatedResponseUtils() {
        throw new IllegalAccessError("This constructor is private");
    }

    @WorkerThread
    @NonNull
    public static <T> List<T> downloadAll(@NonNull BaseFHIRGetPaginatedRequest<T> request) throws Exception {
        return downloadAll(request::get);
    }
    @WorkerThread
    @NonNull
    public static <T> List<T> downloadAll(@NonNull RequestExecutor<T> executor) throws Exception {
        return downloadAll(executor, null);
    }

    @NonNull
    @WorkerThread
    public static <T, U> List<U> downloadAll(
            @NonNull RequestExecutor<T> executor,
            @Nullable Mapper.Transformer<T, U> transformer
    ) throws Exception {
        int page = 0;
        boolean hasMore;
        List<U> list = new ArrayList<>();
        Mapper<T, U> mapper = transformer != null ? new Mapper<>(transformer) : null;
        do {
            PaginatedResponse<T> response = executor.download(page++);
            if (mapper != null) {
                list.addAll(mapper.map(response.getValue()));
            } else {
                list.addAll((Collection<? extends U>) response.getValue());
            }
            hasMore = response.hasMore();
        } while(hasMore);
        return list;
    }

    @NonNull
    @WorkerThread
    public static <T, U> DownloadResult<U> downloadAllSkipFailedPages(
            @NonNull String endpoint,
            @NonNull RequestExecutor<T> executor,
            @Nullable Mapper.Transformer<T, U> transformer,
            int maxPages,
            int maxConsecutiveFailures
    ) throws Exception {
        int page = 0;
        int consecutiveFailures = 0;
        boolean hasMore = true;
        boolean hasSkippedPages = false;
        List<U> list = new ArrayList<>();
        List<Integer> skippedPages = new ArrayList<>();
        List<Integer> recoveredPages = new ArrayList<>();
        Mapper<T, U> mapper = transformer != null ? new Mapper<>(transformer) : null;

        while (hasMore && page < maxPages) {
            final int displayPage = page + 1;
            try {
                PaginatedResponse<T> response = executor.download(page);
                if (mapper != null) {
                    list.addAll(mapper.map(response.getValue()));
                } else {
                    list.addAll((Collection<? extends U>) response.getValue());
                }
                hasMore = response.hasMore();
                consecutiveFailures = 0;
                if (hasSkippedPages) {
                    recoveredPages.add(displayPage);
                }
            } catch (Exception exception) {
                skippedPages.add(displayPage);
                hasSkippedPages = true;
                consecutiveFailures++;
                logSkippedPage(endpoint, displayPage, exception);
                if (consecutiveFailures >= maxConsecutiveFailures) {
                    Log.w(
                            LOG_TAG,
                            String.format(
                                    "endpoint=%s action=stop reason=maxConsecutiveFailures reached skippedPages=%s",
                                    endpoint,
                                    skippedPages
                            )
                    );
                    break;
                }
            }
            page++;
        }

        if (page >= maxPages && hasMore) {
            Log.w(
                    LOG_TAG,
                    String.format(
                            "endpoint=%s action=stop reason=maxPages reached maxPages=%s skippedPages=%s",
                            endpoint,
                            maxPages,
                            skippedPages
                    )
            );
        }

        Log.i(
                LOG_TAG,
                String.format(
                        "endpoint=%s summary skippedPages=%s recoveredPages=%s totalItems=%s",
                        endpoint,
                        skippedPages,
                        recoveredPages,
                        list.size()
                )
        );
        return new DownloadResult<>(list, skippedPages, recoveredPages);
    }

    private static void logSkippedPage(@NonNull String endpoint, int page, @NonNull Exception exception) {
        String errorType = exception.getClass().getSimpleName();
        if (exception instanceof HttpException) {
            HttpException httpException = (HttpException) exception;
            Log.w(
                    LOG_TAG,
                    String.format(
                            "endpoint=%s page=%s action=skip errorType=%s code=%s message=%s",
                            endpoint,
                            page,
                            errorType,
                            httpException.getCode(),
                            exception.getMessage()
                    )
            );
        } else {
            Log.w(
                    LOG_TAG,
                    String.format(
                            "endpoint=%s page=%s action=skip errorType=%s message=%s",
                            endpoint,
                            page,
                            errorType,
                            exception.getMessage()
                    )
            );
        }
    }

    public interface RequestExecutor<T> {

        @NonNull
        @WorkerThread
        PaginatedResponse<T> download(int page) throws Exception;
    }

    public static class DownloadResult<U> {
        @NonNull
        private final List<U> items;
        @NonNull
        private final List<Integer> skippedPages;
        @NonNull
        private final List<Integer> recoveredPages;

        public DownloadResult(
                @NonNull List<U> items,
                @NonNull List<Integer> skippedPages,
                @NonNull List<Integer> recoveredPages
        ) {
            this.items = items;
            this.skippedPages = skippedPages;
            this.recoveredPages = recoveredPages;
        }

        @NonNull
        public List<U> getItems() {
            return items;
        }

        @NonNull
        public List<Integer> getSkippedPages() {
            return Collections.unmodifiableList(skippedPages);
        }

        @NonNull
        public List<Integer> getRecoveredPages() {
            return Collections.unmodifiableList(recoveredPages);
        }

        public boolean hasSkippedPages() {
            return !skippedPages.isEmpty();
        }
    }
}
