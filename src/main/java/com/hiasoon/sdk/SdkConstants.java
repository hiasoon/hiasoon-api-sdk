
package com.hiasoon.sdk;

public class SdkConstants {
    public static final String HEADER_APPID = "APPID";
    public static final String HEADER_TIMESTAMP = "_t";
    public static final String HEADER_NONCE = "_n";
    public static final String HEADER_SIGN = "_s";
    public static final String HEADER_PLATFORM = "X-Platform";
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";

    public static final long DEFAULT_TIMEOUT = 30000;
    public static final String DEFAULT_PLATFORM = "Web";

    public static final String NONCE_CHARSET = "ABCDEFGHJKMNPQRSTWXYZabcdefhijkmnprstwxyz2345678";
    public static final int DEFAULT_NONCE_LENGTH = 6;
}
