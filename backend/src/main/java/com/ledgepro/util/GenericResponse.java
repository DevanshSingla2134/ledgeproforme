package com.ledgepro.util;

import java.time.LocalDateTime;

/**
 * Generic API response wrapper demonstrating Java Generics concept.
 * @param <T> Payload type
 */
public class GenericResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private String errorCode;
    private LocalDateTime timestamp;

    public GenericResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public GenericResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }

    public static <T> GenericResponse<T> success(T data, String message) {
        GenericResponse<T> response = new GenericResponse<>(true, message, data);
        return response;
    }

    public static <T> GenericResponse<T> success(T data) {
        return success(data, "Operation completed successfully");
    }

    public static <T> GenericResponse<T> error(String errorCode, String message) {
        GenericResponse<T> response = new GenericResponse<>(false, message, null);
        response.setErrorCode(errorCode);
        return response;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
