
package com.hiasoon.sdk.model;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class ApiRequest {
    private String url;
    private String method = "POST";
    private Map<String, Object> params = new HashMap<>();
    private Map<String, String> headers = new HashMap<>();
    /**
     * 原始请求体，用于网关透明转发场景
     * 若设置则优先使用，不再使用params序列化
     */
    private String rawBody;
}
