
package com.hiasoon.sdk.model;

import lombok.Data;

@Data
public class ApiResponse<T> {
    private int code;
    private String message;
    private T data;
    private Object info;

    /**
     * HTTP状态码
     */
    private int httpStatusCode;

    /**
     * 错误类型: NETWORK_ERROR / HTTP_ERROR / PARSE_ERROR / SIGN_ERROR / AUTH_ERROR
     */
    private String errorType;

    public boolean isSuccess() {
        return code == 200;
    }

    /**
     * 创建网络错误响应
     */
    public static <T> ApiResponse<T> networkError(String url, String method, String errorDetail) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(-1);
        response.setHttpStatusCode(-1);
        response.setErrorType("NETWORK_ERROR");
        response.setMessage(String.format("[%s %s] 网络错误: %s", method, url, errorDetail));
        return response;
    }

    /**
     * 创建HTTP错误响应
     */
    public static <T> ApiResponse<T> httpError(int httpStatusCode, String url, String method, String errorDetail) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(httpStatusCode);
        response.setHttpStatusCode(httpStatusCode);
        response.setErrorType("HTTP_ERROR");
        response.setMessage(String.format("[%s %s] HTTP %d: %s", method, url, httpStatusCode, errorDetail));
        return response;
    }

    /**
     * 创建解析错误响应
     */
    public static <T> ApiResponse<T> parseError(String url, String method, String errorDetail) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(-1);
        response.setHttpStatusCode(-1);
        response.setErrorType("PARSE_ERROR");
        response.setMessage(String.format("[%s %s] 响应解析失败: %s", method, url, errorDetail));
        return response;
    }

    /**
     * 创建空响应错误
     */
    public static <T> ApiResponse<T> emptyResponse(int httpStatusCode, String url, String method) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(httpStatusCode);
        response.setHttpStatusCode(httpStatusCode);
        response.setErrorType("EMPTY_RESPONSE");
        response.setMessage(String.format("[%s %s] 响应体为空 (HTTP %d)", method, url, httpStatusCode));
        return response;
    }
}
