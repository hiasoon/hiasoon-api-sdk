package com.hiasoon.sdk.model;

import lombok.Data;

import java.util.Map;

/**
 * 原始API响应 - 保存完整的HTTP原始响应
 * 用于网关透明转发场景，需要原封不动地返回给客户端
 */
@Data
public class RawApiResponse {

    /**
     * HTTP状态码
     */
    private int httpStatusCode;

    /**
     * 原始响应体（字符串）
     */
    private String rawBody;

    /**
     * 响应头（允许多值）
     */
    private Map<String, String> headers;

    /**
     * 错误信息（如果有）
     */
    private String errorMessage;

    /**
     * 是否为错误响应
     */
    private boolean error;

    /**
     * 静态工厂方法：成功响应
     */
    public static RawApiResponse success(int httpStatusCode, String rawBody, Map<String, String> headers) {
        RawApiResponse response = new RawApiResponse();
        response.setHttpStatusCode(httpStatusCode);
        response.setRawBody(rawBody);
        response.setHeaders(headers);
        response.setError(false);
        return response;
    }

    /**
     * 静态工厂方法：错误响应
     */
    public static RawApiResponse error(int httpStatusCode, String errorMessage, Map<String, String> headers) {
        RawApiResponse response = new RawApiResponse();
        response.setHttpStatusCode(httpStatusCode);
        response.setErrorMessage(errorMessage);
        response.setHeaders(headers);
        response.setError(true);
        return response;
    }

    /**
     * 静态工厂方法：网络错误
     */
    public static RawApiResponse networkError(String errorMessage) {
        return error(-1, errorMessage, null);
    }
}
