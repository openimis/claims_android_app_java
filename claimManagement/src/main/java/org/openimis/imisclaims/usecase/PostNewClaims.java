package org.openimis.imisclaims.usecase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import org.openimis.imisclaims.domain.entity.PendingClaim;
import org.openimis.imisclaims.network.exception.InsertClaimException;
import org.openimis.imisclaims.network.request.PostNewClaimRequest;

import java.util.ArrayList;
import java.util.List;

public class PostNewClaims {

    @NonNull
    private final PostNewClaimRequest request;

    public PostNewClaims() {
        this(new PostNewClaimRequest());
    }

    public PostNewClaims(
            @NonNull PostNewClaimRequest request
    ) {
        this.request = request;
    }

    @WorkerThread
    public List<Result> execute(@NonNull List<PendingClaim> pendingClaims) throws Exception {
        List<Result> results = new ArrayList<>();
        for (PendingClaim pendingClaim : pendingClaims) {
            try {
                String claimCode = request.post(pendingClaim);
                results.add(
                        new Result(
                                /* claimCode = */ claimCode,
                                /* status = */ Result.Status.SUCCESS,
                                /* message = */ null
                        )
                );
            } catch (InsertClaimException e) {
                results.add(
                        new Result(
                                /* claimCode = */ pendingClaim.getClaimCode(),
                                /* status = */ Result.Status.ERROR, //TODO figure out when it's REJECTED
                                /* message = */ e.getMessage()
                        )
                );
            }
        }
        return results;
    }

    public static class Result {

        @NonNull
        private final String claimCode;
        private final Status status;
        @Nullable
        private final String message;

        public Result(
                @NonNull String claimCode,
                Status status,
                @Nullable String message
        ) {
            this.claimCode = claimCode;
            this.status = status;
            this.message = message;
        }

        @NonNull
        public String getClaimCode() {
            return claimCode;
        }

        public Status getStatus() {
            return status;
        }

        @Nullable
        public String getMessage() {
            return message;
        }

        public enum Status {
            SUCCESS, REJECTED, ERROR
        }
    }
}
