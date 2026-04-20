package org.openimis.imisclaims.network.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.openimis.imisclaims.network.exception.HttpException;
import org.openimis.imisclaims.network.response.PaginatedResponse;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class PaginatedResponseUtilsTest {

    @Test
    public void downloadAllSkipFailedPages_returnsAllItems_whenNoFailure() throws Exception {
        PaginatedResponseUtils.DownloadResult<String> result = PaginatedResponseUtils.downloadAllSkipFailedPages(
                "Medication",
                page -> {
                    if (page == 0) {
                        return new PaginatedResponse<>(Collections.singletonList("a"), true);
                    }
                    return new PaginatedResponse<>(Collections.singletonList("b"), false);
                },
                null,
                100,
                3
        );

        assertEquals(Arrays.asList("a", "b"), result.getItems());
        assertTrue(result.getSkippedPages().isEmpty());
        assertTrue(result.getRecoveredPages().isEmpty());
        assertFalse(result.hasSkippedPages());
    }

    @Test
    public void downloadAllSkipFailedPages_skipsSingleFailedPage_andContinues() throws Exception {
        PaginatedResponseUtils.DownloadResult<String> result = PaginatedResponseUtils.downloadAllSkipFailedPages(
                "Medication",
                page -> {
                    if (page == 0) {
                        return new PaginatedResponse<>(Collections.singletonList("p1"), true);
                    }
                    if (page == 1) {
                        throw new HttpException(500, "server", "error", null);
                    }
                    if (page == 2) {
                        return new PaginatedResponse<>(Collections.singletonList("p3"), false);
                    }
                    return new PaginatedResponse<>(Collections.emptyList(), false);
                },
                null,
                100,
                3
        );

        assertEquals(Arrays.asList("p1", "p3"), result.getItems());
        assertEquals(Collections.singletonList(2), result.getSkippedPages());
        assertEquals(Collections.singletonList(3), result.getRecoveredPages());
        assertTrue(result.hasSkippedPages());
    }

    @Test
    public void downloadAllSkipFailedPages_stopsAfterMaxConsecutiveFailures() throws Exception {
        Map<Integer, Exception> failures = new HashMap<>();
        failures.put(0, new RuntimeException("f1"));
        failures.put(1, new RuntimeException("f2"));
        failures.put(2, new RuntimeException("f3"));

        PaginatedResponseUtils.DownloadResult<String> result = PaginatedResponseUtils.downloadAllSkipFailedPages(
                "Medication",
                page -> {
                    Exception failure = failures.get(page);
                    if (failure != null) {
                        throw failure;
                    }
                    return new PaginatedResponse<>(Collections.singletonList("ok"), false);
                },
                null,
                100,
                3
        );

        assertTrue(result.getItems().isEmpty());
        assertEquals(Arrays.asList(1, 2, 3), result.getSkippedPages());
        assertTrue(result.getRecoveredPages().isEmpty());
    }

    @Test
    public void downloadAllSkipFailedPages_stopsAtMaxPages_whenHasMoreStillTrue() throws Exception {
        PaginatedResponseUtils.DownloadResult<String> result = PaginatedResponseUtils.downloadAllSkipFailedPages(
                "Medication",
                page -> new PaginatedResponse<>(Collections.singletonList("i" + page), true),
                null,
                2,
                3
        );

        assertEquals(Arrays.asList("i0", "i1"), result.getItems());
        assertTrue(result.getSkippedPages().isEmpty());
    }

    @Test
    public void downloadAllSkipFailedPages_recordsRecoveredPages_afterFailure() throws Exception {
        PaginatedResponseUtils.DownloadResult<String> result = PaginatedResponseUtils.downloadAllSkipFailedPages(
                "Medication",
                page -> {
                    if (page == 0) {
                        throw new RuntimeException("fail");
                    }
                    if (page == 1) {
                        return new PaginatedResponse<>(Collections.singletonList("r1"), true);
                    }
                    return new PaginatedResponse<>(Collections.singletonList("r2"), false);
                },
                null,
                100,
                3
        );

        assertEquals(Collections.singletonList(1), result.getSkippedPages());
        assertEquals(Arrays.asList(2, 3), result.getRecoveredPages());
        assertEquals(Arrays.asList("r1", "r2"), result.getItems());
    }

    @Test
    public void downloadResult_listsAreUnmodifiable_forSkippedAndRecovered() {
        PaginatedResponseUtils.DownloadResult<String> result =
                new PaginatedResponseUtils.DownloadResult<>(
                        Collections.singletonList("x"),
                        Collections.singletonList(1),
                        Collections.singletonList(2)
                );

        assertThrows(UnsupportedOperationException.class, () -> result.getSkippedPages().add(3));
        assertThrows(UnsupportedOperationException.class, () -> result.getRecoveredPages().add(4));
    }
}
