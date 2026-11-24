package com.ledgepro.util;

/**
 * Functional result envelope demonstrating Java Generics, static factory methods,
 * and monadic pattern for service operations.
 * @param <T> Success value type
 */
public class GenericResult<T> {
    private final boolean success;
    private final T value;
    private final String error;

    private GenericResult(boolean success, T value, String error) {
        this.success = success;
        this.value = value;
        this.error = error;
    }

    public static <T> GenericResult<T> ok(T value) {
        return new GenericResult<>(true, value, null);
    }

    public static <T> GenericResult<T> fail(String error) {
        return new GenericResult<>(false, null, error);
    }

    public boolean isSuccess() {
        return success;
    }

    public T getValue() {
        return value;
    }

    public String getError() {
        return error;
    }
}
