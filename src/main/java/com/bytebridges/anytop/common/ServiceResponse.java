package com.bytebridges.anytop.common;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServiceResponse<T> {

    private boolean status;
    private Integer statusCode;
    private String message;
    private T data;

    public ServiceResponse(boolean status, Integer statusCode, String message, T data) {
        this.status = status;
        this.statusCode = statusCode;
        this.message = message;
        this.data = data;
    }

    // SUCCESS
    public static <T> ServiceResponse<T> success(T data, String message) {
        return new ServiceResponse<>(true, 200, message, data);
    }

    public static <T> ServiceResponse<T> success(T data) {
        return new ServiceResponse<>(true, 200, "Success", data);
    }

    // ERROR
    public static <T> ServiceResponse<T> error(String message) {
        return new ServiceResponse<>(false, 500, message, null);
    }

    public static <T> ServiceResponse<T> error(Integer statusCode, String message) {
        return new ServiceResponse<>(false, statusCode, message, null);
    }
}