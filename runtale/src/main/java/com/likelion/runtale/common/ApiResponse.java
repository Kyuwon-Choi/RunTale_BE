package com.likelion.runtale.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.likelion.runtale.common.response.ErrorMessage;
import com.likelion.runtale.common.response.SuccessMessage;
import jakarta.annotation.Nullable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiResponse<T> {

    private final int status;
    private final String message;
    @JsonInclude(NON_NULL)
    private T data;

    private ApiResponse(){
        throw new IllegalStateException();
    }

    public static <T> ApiResponse<T> success(SuccessMessage success) {
        return new ApiResponse<>(success.getHttpStatusValue(), success.getMessage());
    }

    public static <T> ApiResponse<T> success(SuccessMessage success, T data){
        return new ApiResponse<>(success.getHttpStatusValue(), success.getMessage(), data);
    }

    public static <T> ApiResponse<T> error(ErrorMessage error){
        return new ApiResponse<>(error.getHttpStatusValue(), error.getMessage());
    }

    public static <T> ApiResponse<T> error(ErrorMessage error, @Nullable String message){
        return new ApiResponse<>(error.getHttpStatusValue(), message);
    }

    public static <T> ApiResponse<T> error(ErrorMessage error, @Nullable String message, @Nullable T data){
        return new ApiResponse<>(error.getHttpStatusValue(), message, data);
    }

}
