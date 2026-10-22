package com.hiasoon.sdk.api;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.hiasoon.sdk.SdkConstants;
import com.hiasoon.sdk.model.ApiRequest;
import com.hiasoon.sdk.model.ApiResponse;
import com.hiasoon.sdk.model.RawApiResponse;
import com.hiasoon.sdk.sign.NonceGenerator;
import com.hiasoon.sdk.sign.Sign;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 氦闪平台 API 客户端
 * 提供 HTTP 请求、签名、响应解析等基础能力
 * 
 * 使用场景：
 * 1. 普通 API 调用：post/get 方法
 * 2. 网关透明转发：forward/forwardRaw 方法
 * 
 * 认证信息由调用方通过自定义请求头传递
 */
@Slf4j
public class HiasoonApiClient {
    private final HiasoonApiConfig config;
    private final RestTemplate restTemplate;

    public HiasoonApiClient(HiasoonApiConfig config) {
        this.config = config;
        this.restTemplate = new RestTemplate();
    }

    /**
     * POST 请求
     */
    public ApiResponse<JSONObject> post(String url, Map<String, Object> params) {
        return execute(buildRequest(url, "POST", params, null, null));
    }

    /**
     * POST 请求（带自定义请求头）
     */
    public ApiResponse<JSONObject> post(String url, Map<String, Object> params, Map<String, String> headers) {
        return execute(buildRequest(url, "POST", params, headers, null));
    }

    /**
     * GET 请求
     */
    public ApiResponse<JSONObject> get(String url, Map<String, Object> params) {
        return execute(buildRequest(url, "GET", params, null, null));
    }

    /**
     * GET 请求（带自定义请求头）
     */
    public ApiResponse<JSONObject> get(String url, Map<String, Object> params, Map<String, String> headers) {
        return execute(buildRequest(url, "GET", params, headers, null));
    }

    /**
     * 网关透明转发（解析响应后返回）
     *
     * @param url           请求URL
     * @param method        请求方法（GET/POST）
     * @param params        业务参数（用于签名）
     * @param customHeaders 自定义请求头（如 Authorization 等认证信息）
     * @param rawBody       原始请求体（优先使用，用于转发场景）
     * @return API响应
     */
    public ApiResponse<JSONObject> forward(String url, String method, Map<String, Object> params,
                                            Map<String, String> customHeaders, String rawBody) {
        return execute(buildRequest(url, method, params, customHeaders, rawBody));
    }

    /**
     * 网关透明转发（原封不动返回原始响应）
     *
     * @param url           请求URL
     * @param method        请求方法（GET/POST）
     * @param params        业务参数（用于签名）
     * @param customHeaders 自定义请求头（如 Authorization 等认证信息）
     * @param rawBody       原始请求体（优先使用，用于转发场景）
     * @return 原始API响应对象
     */
    public RawApiResponse forwardRaw(String url, String method, Map<String, Object> params,
                                      Map<String, String> customHeaders, String rawBody) {
        return executeRaw(buildRequest(url, method, params, customHeaders, rawBody));
    }

    private ApiRequest buildRequest(String url, String method, Map<String, Object> params,
                                    Map<String, String> customHeaders, String rawBody) {
        ApiRequest request = new ApiRequest();
        request.setUrl(url);
        request.setMethod(method);
        if (params != null) {
            request.setParams(params);
        }
        if (customHeaders != null) {
            request.setHeaders(customHeaders);
        }
        if (rawBody != null && !rawBody.isEmpty()) {
            request.setRawBody(rawBody);
        }
        return request;
    }

    private ApiResponse<JSONObject> execute(ApiRequest apiRequest) {
        String fullUrl = buildFullUrl(apiRequest.getUrl());
        String method = apiRequest.getMethod();

        try {
            HttpHeaders headers = buildRequestHeaders(apiRequest.getParams(), apiRequest.getHeaders());

            String body = apiRequest.getRawBody();
            if (body == null || body.isEmpty()) {
                body = JSONUtil.toJsonStr(apiRequest.getParams());
            }

            HttpEntity<String> entity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response;
            if ("GET".equalsIgnoreCase(method)) {
                response = restTemplate.exchange(fullUrl, HttpMethod.GET, entity, String.class);
            } else {
                response = restTemplate.exchange(fullUrl, HttpMethod.POST, entity, String.class);
            }

            return parseResponse(response, fullUrl, method);

        } catch (HttpClientErrorException e) {
            log.warn("客户端错误: {} {} -> HTTP {}: {}", method, fullUrl, e.getStatusCode().value(), e.getMessage());
            String errorBody = e.getResponseBodyAsString();
            return parseErrorResponse(e.getStatusCode().value(), fullUrl, method, errorBody, e.getMessage());

        } catch (HttpServerErrorException e) {
            log.error("服务端错误: {} {} -> HTTP {}: {}", method, fullUrl, e.getStatusCode().value(), e.getMessage());
            String errorBody = e.getResponseBodyAsString();
            return parseErrorResponse(e.getStatusCode().value(), fullUrl, method, errorBody, e.getMessage());

        } catch (RestClientException e) {
            log.error("网络异常: {} {} -> {}", method, fullUrl, e.getMessage());
            return ApiResponse.networkError(fullUrl, method, e.getMessage());

        } catch (Exception e) {
            log.error("未知异常: {} {} -> {}", method, fullUrl, e.getMessage(), e);
            return ApiResponse.networkError(fullUrl, method, "未知错误: " + e.getMessage());
        }
    }

    private RawApiResponse executeRaw(ApiRequest apiRequest) {
        String fullUrl = buildFullUrl(apiRequest.getUrl());
        String method = apiRequest.getMethod();

        try {
            HttpHeaders headers = buildRequestHeaders(apiRequest.getParams(), apiRequest.getHeaders());

            String body = apiRequest.getRawBody();
            if (body == null || body.isEmpty()) {
                body = JSONUtil.toJsonStr(apiRequest.getParams());
            }

            HttpEntity<String> entity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response;
            if ("GET".equalsIgnoreCase(method)) {
                response = restTemplate.exchange(fullUrl, HttpMethod.GET, entity, String.class);
            } else {
                response = restTemplate.exchange(fullUrl, HttpMethod.POST, entity, String.class);
            }

            Map<String, String> responseHeaders = convertHeaders(response.getHeaders());
            return RawApiResponse.success(
                    response.getStatusCode().value(),
                    response.getBody(),
                    responseHeaders
            );

        } catch (HttpClientErrorException e) {
            log.warn("客户端错误: {} {} -> HTTP {}: {}", method, fullUrl, e.getStatusCode().value(), e.getMessage());
            Map<String, String> responseHeaders = convertHeaders(e.getResponseHeaders());
            return RawApiResponse.error(
                    e.getStatusCode().value(),
                    e.getResponseBodyAsString(),
                    responseHeaders
            );

        } catch (HttpServerErrorException e) {
            log.error("服务端错误: {} {} -> HTTP {}: {}", method, fullUrl, e.getStatusCode().value(), e.getMessage());
            Map<String, String> responseHeaders = convertHeaders(e.getResponseHeaders());
            return RawApiResponse.error(
                    e.getStatusCode().value(),
                    e.getResponseBodyAsString(),
                    responseHeaders
            );

        } catch (RestClientException e) {
            log.error("网络异常: {} {} -> {}", method, fullUrl, e.getMessage());
            return RawApiResponse.networkError(e.getMessage());

        } catch (Exception e) {
            log.error("未知异常: {} {} -> {}", method, fullUrl, e.getMessage(), e);
            return RawApiResponse.networkError("未知错误: " + e.getMessage());
        }
    }

    /**
     * 构建请求头
     * 1. 设置 Content-Type、APPID、X-Platform
     * 2. 透传自定义请求头（如 Authorization 等）
     * 3. 添加签名头（_t, _n, _s）
     */
    private HttpHeaders buildRequestHeaders(Map<String, Object> params, Map<String, String> customHeaders) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(SdkConstants.HEADER_APPID, config.getAppId());
        headers.set(SdkConstants.HEADER_PLATFORM, config.getPlatform());

        // 透传自定义请求头
        if (customHeaders != null) {
            List<String> proxyHeaders = config.getProxyHeaders();
            for (Map.Entry<String, String> entry : customHeaders.entrySet()) {
                if (proxyHeaders == null || proxyHeaders.isEmpty() || isProxyHeader(entry.getKey(), proxyHeaders)) {
                    headers.set(entry.getKey(), entry.getValue());
                }
            }
        }

        // 生成签名
        long timestamp = System.currentTimeMillis();
        String nonce = NonceGenerator.generateNonce();
        String sign = null;

        try {
            sign = Sign.generateHiasoonSign(params, timestamp, nonce, config.getAppSecret());
        } catch (Exception e) {
            log.error("签名生成失败: {}", e.getMessage());
        }

        headers.set(SdkConstants.HEADER_TIMESTAMP, String.valueOf(timestamp));
        headers.set(SdkConstants.HEADER_NONCE, nonce);
        if (sign != null) {
            headers.set(SdkConstants.HEADER_SIGN, sign);
        }

        return headers;
    }

    /**
     * 判断请求头是否允许透传（不区分大小写）
     */
    private boolean isProxyHeader(String headerName, List<String> proxyHeaders) {
        for (String allowed : proxyHeaders) {
            if (allowed != null && allowed.equalsIgnoreCase(headerName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 转换 Spring HttpHeaders 为 Map<String, String>
     */
    private Map<String, String> convertHeaders(HttpHeaders headers) {
        Map<String, String> result = new LinkedHashMap<>();
        if (headers != null) {
            for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
                List<String> values = entry.getValue();
                if (values != null && !values.isEmpty()) {
                    result.put(entry.getKey(), values.get(0));
                }
            }
        }
        return result;
    }

    private String buildFullUrl(String url) {
        if (url.startsWith("http")) {
            return url;
        }
        String baseUrl = config.getBaseUrl();
        if (!baseUrl.endsWith("/")) {
            baseUrl = baseUrl + "/";
        }
        if (url.startsWith("/")) {
            url = url.substring(1);
        }
        return baseUrl + url;
    }

    private ApiResponse<JSONObject> parseErrorResponse(int httpStatusCode, String url, String method,
                                                        String errorBody, String defaultMessage) {
        if (errorBody != null && !errorBody.isEmpty()) {
            try {
                JSONObject jsonObj = JSONUtil.parseObj(errorBody);
                if (jsonObj.containsKey("code") && jsonObj.containsKey("message")) {
                    ApiResponse<JSONObject> response = new ApiResponse<>();
                    response.setCode(jsonObj.getInt("code"));
                    response.setMessage(jsonObj.getStr("message"));
                    response.setHttpStatusCode(httpStatusCode);
                    response.setErrorType("HTTP_ERROR");
                    response.setData(jsonObj.getJSONObject("data"));
                    response.setInfo(jsonObj.get("info"));
                    return response;
                }
            } catch (Exception ignored) {
            }
        }
        return ApiResponse.httpError(httpStatusCode, url, method,
                errorBody != null ? errorBody : defaultMessage);
    }

    private ApiResponse<JSONObject> parseResponse(ResponseEntity<String> response, String url, String method) {
        int httpStatusCode = response.getStatusCode().value();
        String body = response.getBody();

        if (body == null || body.isEmpty()) {
            log.warn("响应体为空: {} {} -> HTTP {}", method, url, httpStatusCode);
            return ApiResponse.emptyResponse(httpStatusCode, url, method);
        }

        try {
            JSONObject jsonObj = JSONUtil.parseObj(body);

            if (jsonObj.containsKey("code")) {
                ApiResponse<JSONObject> apiResponse = new ApiResponse<>();
                apiResponse.setCode(jsonObj.getInt("code"));
                apiResponse.setMessage(jsonObj.getStr("message"));
                apiResponse.setHttpStatusCode(httpStatusCode);

                if (jsonObj.containsKey("data")) {
                    Object dataObj = jsonObj.get("data");
                    if (dataObj instanceof JSONObject) {
                        apiResponse.setData((JSONObject) dataObj);
                    } else if (dataObj != null) {
                        apiResponse.setData(JSONUtil.parseObj(JSONUtil.toJsonStr(dataObj)));
                    }
                }

                if (jsonObj.containsKey("info")) {
                    apiResponse.setInfo(jsonObj.get("info"));
                }

                if (jsonObj.getInt("code") != 200) {
                    log.warn("业务错误: {} {} -> code={}, message={}",
                            method, url, apiResponse.getCode(), apiResponse.getMessage());
                }

                return apiResponse;
            }

            ApiResponse<JSONObject> apiResponse = new ApiResponse<>();
            apiResponse.setCode(httpStatusCode >= 200 && httpStatusCode < 300 ? 200 : httpStatusCode);
            apiResponse.setMessage("success");
            apiResponse.setHttpStatusCode(httpStatusCode);
            apiResponse.setData(jsonObj);
            return apiResponse;

        } catch (Exception e) {
            log.error("响应解析失败: {} {} -> {}", method, url, e.getMessage());
            return ApiResponse.parseError(url, method, e.getMessage());
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final HiasoonApiConfig config = new HiasoonApiConfig();

        public Builder appId(String appId) {
            config.setAppId(appId);
            return this;
        }

        public Builder appSecret(String appSecret) {
            config.setAppSecret(appSecret);
            return this;
        }

        public Builder baseUrl(String baseUrl) {
            config.setBaseUrl(baseUrl);
            return this;
        }

        public Builder timeout(long timeout) {
            config.setTimeout(timeout);
            return this;
        }

        public Builder platform(String platform) {
            config.setPlatform(platform);
            return this;
        }

        public Builder proxyHeaders(List<String> proxyHeaders) {
            config.setProxyHeaders(proxyHeaders);
            return this;
        }

        public HiasoonApiClient build() {
            return new HiasoonApiClient(config);
        }
    }
}
