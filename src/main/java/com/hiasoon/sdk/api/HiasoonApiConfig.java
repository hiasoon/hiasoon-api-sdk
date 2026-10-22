
package com.hiasoon.sdk.api;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Data
@ConfigurationProperties(prefix = "hiasoon.api")
public class HiasoonApiConfig {
    private String appId;
    private String appSecret;
    private String baseUrl;
    private long timeout = 30000;
    private String platform = "Web";
    /**
     * 允许透传的请求头keys（不区分大小写）
     * 只有在此列表中的请求头才会被透传到氦闪平台
     */
    private List<String> proxyHeaders;
}
