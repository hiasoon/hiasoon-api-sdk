# 氦闪平台API SDK - Java客户端

[![Maven Central](https://img.shields.io/maven-central/v/com.hiasoon/hiasoon-api-sdk.svg)](https://search.maven.org/search?q=g:com.hiasoon)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

氦闪平台API SDK是一个用于调用氦闪开放API的Java客户端库，提供了签名生成、HTTP请求封装、响应解析等功能，帮助开发者快速接入氦闪平台。

## 功能特性

- ✅ **自动签名**：自动生成时间戳、Nonce和HMAC-SHA256签名
- ✅ **参数标准化**：支持嵌套对象、数组的参数排序和标准化
- ✅ **网关透明转发**：支持将请求原封不动地转发到氦闪平台，响应可选择解析或原样返回
- ✅ **请求头透传**：支持通过配置白名单透传自定义请求头（如Authorization等认证信息）
- ✅ **Spring Boot集成**：支持自动配置，开箱即用
- ✅ **响应解析**：自动解析API响应为统一格式，支持原始响应获取
- ✅ **Builder模式**：支持链式配置，灵活使用
- ✅ **签名验证**：提供签名校验和时间戳有效性验证工具方法

## 快速开始

### 环境要求

- JDK 8+（Builder 模式）
- JDK 11+（Spring Boot 自动配置模式，因 Spring Boot 2.7.x 最低要求）
- Maven 3.6+

### 安装依赖

#### Maven

在 `pom.xml` 中添加依赖：

```xml
<dependency>
    <groupId>com.hiasoon</groupId>
    <artifactId>hiasoon-api-sdk</artifactId>
    <version>1.0.0</version>
</dependency>
```

#### Gradle

在 `build.gradle` 中添加依赖：

```groovy
implementation 'com.hiasoon:hiasoon-api-sdk:1.0.0'
```

### 使用方式

#### 方式一：Builder模式（非Spring环境）

```java
import com.hiasoon.sdk.api.HiasoonApiClient;
import com.hiasoon.sdk.model.ApiResponse;
import cn.hutool.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class Demo {
    public static void main(String[] args) {
        // 创建客户端
        HiasoonApiClient client = HiasoonApiClient.builder()
                .appId("APPID_YOUR_APP_ID_HERE")
                .appSecret("your-app-secret")
                .baseUrl("https://api.hiasoon.com")
                .platform("Web")
                .build();

        // 构建请求参数
        Map<String, Object> params = new HashMap<>();
        params.put("type", "developer");
        params.put("key1", "value1");
        params.put("key2", Arrays.asList("a", "b", "c"));

        // 发送POST请求
        ApiResponse<JSONObject> response = client.post("/api/developer-service/user/test", params);

        // 处理响应
        if (response.isSuccess()) {
            System.out.println("请求成功: " + response.getData());
        } else {
            System.out.println("请求失败: " + response.getMessage());
        }
    }
}
```

#### 方式二：Spring Boot自动配置

**1. 配置application.yml**

```yaml
hiasoon:
  api:
    app-id: APPID_YOUR_APP_ID_HERE
    app-secret: your-app-secret
    base-url: https://api.hiasoon.com
    timeout: 30000
    platform: Web
    proxy-headers:
      - Authorization
```

**2. 注入使用**

```java
import com.hiasoon.sdk.api.HiasoonApiClient;
import com.hiasoon.sdk.model.ApiResponse;
import cn.hutool.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
public class ApiService {
    
    @Autowired
    private HiasoonApiClient apiClient;
    
    public void callApi() {
        Map<String, Object> params = new HashMap<>();
        params.put("type", "developer");
        
        ApiResponse<JSONObject> response = apiClient.post("/api/developer-service/user/test", params);
        
        if (response.isSuccess()) {
            JSONObject data = response.getData();
            // 处理业务数据
        }
    }
}
```

## API文档

### HiasoonApiClient

#### 构造方法

```java
// 通过配置对象创建
HiasoonApiClient client = new HiasoonApiClient(config);

// 通过Builder创建
HiasoonApiClient client = HiasoonApiClient.builder()
    .appId(String)              // 必填：应用ID
    .appSecret(String)          // 必填：应用密钥
    .baseUrl(String)            // 必填：API基础URL
    .timeout(long)              // 可选：超时时间(ms)，默认30000
    .platform(String)           // 可选：平台标识，默认Web
    .proxyHeaders(List<String>) // 可选：允许透传的请求头白名单
    .build();
```

#### 请求方法

| 方法 | 说明 | 参数 | 返回值 |
|------|------|------|--------|
| `post(url, params)` | 发送POST请求 | url: 接口路径, params: 请求参数 | `ApiResponse<JSONObject>` |
| `post(url, params, headers)` | 发送POST请求（带自定义请求头） | url: 接口路径, params: 请求参数, headers: 自定义请求头 | `ApiResponse<JSONObject>` |
| `get(url, params)` | 发送GET请求 | url: 接口路径, params: 请求参数 | `ApiResponse<JSONObject>` |
| `get(url, params, headers)` | 发送GET请求（带自定义请求头） | url: 接口路径, params: 请求参数, headers: 自定义请求头 | `ApiResponse<JSONObject>` |

#### 网关转发方法

| 方法 | 说明 | 参数 | 返回值 |
|------|------|------|--------|
| `forward(url, method, params, headers, rawBody)` | 网关透明转发（解析响应） | url: 接口路径, method: GET/POST, params: 业务参数, headers: 自定义请求头, rawBody: 原始请求体 | `ApiResponse<JSONObject>` |
| `forwardRaw(url, method, params, headers, rawBody)` | 网关透明转发（返回原始响应） | url: 接口路径, method: GET/POST, params: 业务参数, headers: 自定义请求头, rawBody: 原始请求体 | `RawApiResponse` |

**参数说明：**
- `rawBody`：原始请求体（优先使用，用于转发场景）。若设置则直接使用此内容作为请求体，不再序列化 `params`
- `headers`：自定义请求头，只有在 `proxyHeaders` 白名单中的请求头才会被透传

### ApiResponse

```java
public class ApiResponse<T> {
    private int code;              // 业务响应码，200表示成功
    private String message;        // 响应消息
    private T data;                // 响应数据
    private Object info;           // 附加信息
    private int httpStatusCode;    // HTTP状态码
    private String errorType;      // 错误类型：NETWORK_ERROR / HTTP_ERROR / PARSE_ERROR / EMPTY_RESPONSE
    
    public boolean isSuccess();    // 判断是否成功(code == 200)
    
    // 静态工厂方法
    public static <T> ApiResponse<T> networkError(url, method, errorDetail);
    public static <T> ApiResponse<T> httpError(httpStatusCode, url, method, errorDetail);
    public static <T> ApiResponse<T> parseError(url, method, errorDetail);
    public static <T> ApiResponse<T> emptyResponse(httpStatusCode, url, method);
}
```

### RawApiResponse

```java
public class RawApiResponse {
    private int httpStatusCode;         // HTTP状态码
    private String rawBody;             // 原始响应体（字符串）
    private Map<String, String> headers; // 响应头
    private String errorMessage;        // 错误信息（如果有）
    private boolean error;              // 是否为错误响应
    
    // 静态工厂方法
    public static RawApiResponse success(httpStatusCode, rawBody, headers);
    public static RawApiResponse error(httpStatusCode, errorMessage, headers);
    public static RawApiResponse networkError(errorMessage);
}
```

### ApiRequest

```java
public class ApiRequest {
    private String url;                           // 请求URL
    private String method = "POST";               // 请求方法
    private Map<String, Object> params;           // 请求参数（用于签名和序列化）
    private Map<String, String> headers;          // 请求头
    private String rawBody;                       // 原始请求体（优先使用，用于转发场景）
}
```

## 签名原理

### 签名流程

1. **参数标准化**：对业务参数进行ASCII升序排序，递归处理嵌套对象和数组
2. **拼接字符串**：`key1=value1&key2=value2_t=timestamp_n=nonce`
3. **HMAC-SHA256加密**：使用appSecret对拼接后的字符串进行加密
4. **生成签名**：将加密结果转换为小写十六进制字符串

### 请求头说明

| 字段 | 说明 | 示例 |
|------|------|------|
| `APPID` | 应用ID | APPID_YOUR_APP_ID_HERE |
| `X-Platform` | 平台标识 | Web / App / MiniApp |
| `_t` | 时间戳(毫秒) | 1672531200000 |
| `_n` | 随机字符串(6位) | aB3kM7 |
| `_s` | HMAC-SHA256签名 | 5f4dcc3b5aa765d61d8327deb882cf99 |
| `Authorization` | 用户Token（由调用方透传） | Bearer eyJhbGciOiJIUzI1NiIs... |

### 参数标准化规则

- **空值过滤**：null、空字符串、空数组、空对象不参与签名
- **排序规则**：按Key的ASCII升序排序（大小写敏感）
- **嵌套对象**：递归处理，拼接为 `key=subKey1=subValue1&subKey2=subValue2`
- **数组处理**：元素标准化后排序，用逗号拼接
- **布尔值**：转换为小写字符串（true/false）
- **数字类型**：使用BigDecimal去除尾随零

### 签名验证工具

```java
// 验证签名是否正确
boolean valid = Sign.verifyHiasoonSign(params, timestamp, nonce, sign, appSecret);

// 验证时间戳是否在有效期内（默认5分钟）
boolean valid = Sign.isTimestampValid(timestamp);

// 验证时间戳是否在指定有效期内
boolean valid = Sign.isTimestampValid(timestamp, maxAge);
```

## 配置说明

### 配置项

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `hiasoon.api.app-id` | String | - | 应用ID（必填） |
| `hiasoon.api.app-secret` | String | - | 应用密钥（必填） |
| `hiasoon.api.base-url` | String | - | API基础URL（必填） |
| `hiasoon.api.timeout` | long | 30000 | 请求超时时间(毫秒) |
| `hiasoon.api.platform` | String | Web | 平台标识 |
| `hiasoon.api.proxy-headers` | List<String> | - | 允许透传的请求头白名单（不区分大小写） |

### 完整配置示例

```yaml
hiasoon:
  api:
    app-id: APPID_YOUR_APP_ID_HERE
    app-secret: your-app-secret-123456
    base-url: https://api.hiasoon.com
    timeout: 30000
    platform: Web
    proxy-headers:
      - Authorization
      - X-Custom-Header
```

## 请求头透传说明

SDK 采用网关透明转发模式，认证信息（如 Authorization Token）由调用方通过自定义请求头传递。只有在 `proxy-headers` 白名单中的请求头才会被透传到氦闪平台。

**示例：透传 Authorization 头**

```java
// Builder模式
List<String> proxyHeaders = Arrays.asList("Authorization");
HiasoonApiClient client = HiasoonApiClient.builder()
        .appId("your-app-id")
        .appSecret("your-app-secret")
        .baseUrl("https://api.hiasoon.com")
        .proxyHeaders(proxyHeaders)
        .build();

// 发送请求时传递 Authorization 头
Map<String, String> headers = new HashMap<>();
headers.put("Authorization", "Bearer eyJhbGciOiJIUzI1NiIs...");

Map<String, Object> params = new HashMap<>();
params.put("type", "developer");

ApiResponse<JSONObject> response = client.post("/api/developer-service/user/test", params, headers);
```

## 示例代码

### 示例1：普通POST请求

```java
Map<String, Object> params = new HashMap<>();
params.put("type", "developer");
params.put("key1", "value1");

ApiResponse<JSONObject> response = client.post("/api/developer-service/user/test", params);

if (response.isSuccess()) {
    System.out.println("请求成功: " + response.getData());
} else {
    System.out.println("请求失败: " + response.getMessage());
}
```

### 示例2：带认证的请求

```java
// 配置允许透传Authorization头
// Spring Boot配置: hiasoon.api.proxy-headers: [Authorization]

Map<String, String> headers = new HashMap<>();
headers.put("Authorization", "Bearer eyJhbGciOiJIUzI1NiIs...");

Map<String, Object> params = new HashMap<>();
params.put("type", "developer");

ApiResponse<JSONObject> response = client.post("/api/developer-service/user/info", params, headers);
```

### 示例3：发送GET请求

```java
Map<String, Object> params = new HashMap<>();
params.put("appid", "your-app-id");

ApiResponse<JSONObject> response = client.get("/api/developer-service/app/getOne", params);

if (response.isSuccess()) {
    JSONObject data = response.getData();
    String appName = data.getStr("name");
    System.out.println("应用名称: " + appName);
}
```

### 示例4：网关透明转发（解析响应）

```java
// forward方法：适用于需要解析标准响应格式的场景
Map<String, Object> params = new HashMap<>();
params.put("type", "developer");

Map<String, String> headers = new HashMap<>();
headers.put("Authorization", "Bearer xxx");

// 有原始请求体时直接使用rawBody，不再序列化params
String rawBody = "{\"type\":\"developer\",\"custom\":\"value\"}";

ApiResponse<JSONObject> response = client.forward(
        "/api/developer-service/custom/endpoint",  // url
        "POST",                                     // method
        params,                                     // 用于签名的业务参数
        headers,                                    // 自定义请求头
        rawBody                                     // 原始请求体（优先使用）
);
```

### 示例5：网关透明转发（原始响应）

```java
// forwardRaw方法：适用于需要完全控制响应的场景（如直接转发给前端）
Map<String, Object> params = new HashMap<>();
params.put("type", "developer");

Map<String, String> headers = new HashMap<>();
headers.put("Authorization", "Bearer xxx");

String rawBody = "{\"type\":\"developer\",\"custom\":\"value\"}";

RawApiResponse response = client.forwardRaw(
        "/api/developer-service/proxy/endpoint",
        "POST",
        params,
        headers,
        rawBody
);

if (!response.isError()) {
    System.out.println("HTTP状态码: " + response.getHttpStatusCode());
    System.out.println("响应体: " + response.getRawBody());
    System.out.println("响应头: " + response.getHeaders());
} else {
    System.out.println("请求失败: " + response.getErrorMessage());
}
```

### 示例6：复杂参数请求

```java
Map<String, Object> params = new HashMap<>();
params.put("appid", "APPID_xxx");
params.put("pageNum", 1);
params.put("pageSize", 10);

// 嵌套对象
Map<String, Object> filter = new HashMap<>();
filter.put("status", "normal");
filter.put("keywords", "test");
params.put("filter", filter);

// 数组参数
params.put("ids", Arrays.asList(1, 2, 3));

ApiResponse<JSONObject> response = client.post("/api/developer-service/app/list", params);
```

### 示例7：错误处理

```java
ApiResponse<JSONObject> response = client.post("/api/developer-service/user/test", params);

if (response.isSuccess()) {
    // 业务成功
    JSONObject data = response.getData();
} else {
    // 根据错误类型处理
    switch (response.getErrorType()) {
        case "NETWORK_ERROR":
            System.out.println("网络错误，请检查网络连接");
            break;
        case "HTTP_ERROR":
            System.out.println("HTTP错误，状态码: " + response.getHttpStatusCode());
            break;
        case "PARSE_ERROR":
            System.out.println("响应解析失败");
            break;
        case "EMPTY_RESPONSE":
            System.out.println("响应体为空");
            break;
        default:
            System.out.println("业务错误: " + response.getMessage());
    }
}
```

## 项目结构

```
hiasoon-api-sdk/
├── src/main/java/com/hiasoon/sdk/
│   ├── SdkConstants.java              # SDK常量定义
│   ├── api/
│   │   ├── HiasoonApiClient.java      # HTTP客户端核心类
│   │   ├── HiasoonApiConfig.java      # 配置属性类
│   │   └── HiasoonApiAutoConfiguration.java  # Spring Boot自动配置
│   ├── model/
│   │   ├── ApiRequest.java            # 请求模型
│   │   ├── ApiResponse.java           # 响应模型（解析后）
│   │   └── RawApiResponse.java        # 原始响应模型（用于转发场景）
│   └── sign/
│       ├── Sign.java                  # 签名核心算法（含验证方法）
│       ├── HmacSHA256Util.java        # HMAC-SHA256工具
│       └── NonceGenerator.java        # Nonce生成器
├── src/main/resources/META-INF/
│   └── spring.factories               # Spring自动配置入口
└── pom.xml                            # Maven配置文件
```

## 构建与发布

### 编译

```bash
mvn compile
```

### 打包

```bash
mvn package -DskipTests
```

### 发布到本地仓库

```bash
mvn install -DskipTests
```

### 发布到Maven中央仓库

```bash
mvn deploy -DskipTests
```

当前版本已发布至 Maven Central，可直接引用：

```xml
<dependency>
    <groupId>com.hiasoon</groupId>
    <artifactId>hiasoon-api-sdk</artifactId>
    <version>1.0.0</version>
</dependency>
```

发布记录：https://repo1.maven.org/maven2/com/hiasoon/hiasoon-api-sdk/1.0.0/

## 注意事项

1. **appSecret保密**：请勿将appSecret硬编码在代码中，建议通过配置文件或环境变量注入
2. **时间同步**：确保客户端与服务器时间同步，时间差超过5分钟会导致签名验证失败
3. **参数类型**：数字类型请使用Long/Integer/Double，避免使用BigDecimal直接传入
4. **URL格式**：baseUrl不需要末尾斜杠，接口路径支持以`/`开头或不开头
5. **请求头透传**：只有在 `proxy-headers` 白名单中的请求头才会被透传，用于认证的 `Authorization` 头需要配置在此列表中
6. **转发场景**：使用 `forward` 或 `forwardRaw` 方法时，`rawBody` 参数会优先于 `params` 序列化结果作为请求体

## License

MIT License

## 联系我们

如有问题或建议，请联系氦闪平台技术支持团队。