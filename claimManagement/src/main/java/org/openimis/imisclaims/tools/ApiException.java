package org.openimis.imisclaims.tools;

/**
 * Custom exception to manage API exceptions
 */
public class ApiException extends Exception {
    private String function;

    public ApiException(String message) {
        super(message);
    }

    public ApiException(String message, String function) {
        super(message);
        this.function = function;
    }

    public String getFunction() {
        return function;
    }
}
