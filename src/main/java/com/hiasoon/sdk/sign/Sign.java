
package com.hiasoon.sdk.sign;

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.Map.Entry;

public class Sign {
    private static final Charset UTF8 = StandardCharsets.UTF_8;

    public static String generateHiasoonSign(Map<String, Object> bizParams, long timestamp, String nonce, String secretKey) throws Exception {
        TreeMap<String, String> sortedParams = standardizeParams(bizParams);
        String paramStr = buildParamStr(sortedParams);
        String signData = paramStr + "_t=" + timestamp + "_n=" + nonce;
        return hmacSha256Hex(signData, secretKey);
    }

    private static TreeMap<String, String> standardizeParams(Map<String, Object> bizParams) {
        TreeMap<String, String> sortedMap = new TreeMap<>(String::compareTo);
        if (bizParams == null || bizParams.isEmpty()) {
            return sortedMap;
        }
        for (Entry<String, Object> entry : bizParams.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value == null) continue;
            if (value instanceof String && ((String) value).trim().isEmpty()) continue;
            if (value instanceof List && ((List<?>) value).isEmpty()) continue;
            if (value instanceof Map && ((Map<?, ?>) value).isEmpty()) continue;

            String stdValue = standardizeValue(value);

            if (stdValue.isEmpty() || "null".equalsIgnoreCase(stdValue)) {
                continue;
            }
            sortedMap.put(key, stdValue);
        }
        return sortedMap;
    }

    private static String standardizeValue(Object value) {
        if (value == null) return "";

        if (value instanceof Map) {
            return buildParamStr(standardizeParams((Map<String, Object>) value));
        }

        if (value instanceof List) {
            List<Object> list = (List<Object>) value;
            List<String> stdList = new ArrayList<>();
            for (Object item : list) {
                String stdItem = standardizeValue(item);
                if (stdItem.isEmpty() || "null".equalsIgnoreCase(stdItem)) {
                    continue;
                }
                stdList.add(stdItem);
            }
            if (stdList.isEmpty()) {
                return "";
            }
            Collections.sort(stdList);
            return String.join(",", stdList);
        }

        if (value instanceof Boolean) {
            return ((Boolean) value).toString().toLowerCase();
        }

        if (value instanceof Number) {
            BigDecimal bd = new BigDecimal(value.toString());
            return bd.stripTrailingZeros().toPlainString();
        }

        String strValue = value.toString().trim();
        return "null".equalsIgnoreCase(strValue) ? "" : strValue;
    }

    private static String buildParamStr(TreeMap<String, String> requestParams) {
        StringBuilder retStr = new StringBuilder();
        Iterator<String> it = requestParams.keySet().iterator();
        while (it.hasNext()) {
            String key = it.next();
            String value = requestParams.get(key);

            if (value == null || value.isEmpty() || "null".equalsIgnoreCase(value)) {
                continue;
            }

            retStr.append(key).append('=').append(value);

            if (it.hasNext()) {
                retStr.append('&');
            }
        }
        return retStr.toString();
    }

    /**
     * 验证氦闪签名
     *
     * @param bizParams  业务参数（用于重放签名）
     * @param timestamp 时间戳（毫秒）
     * @param nonce      随机数
     * @param sign       待验证的签名
     * @param secretKey  密钥
     * @return 签名是否有效
     */
    public static boolean verifyHiasoonSign(Map<String, Object> bizParams, long timestamp,
                                             String nonce, String sign, String secretKey) {
        try {
            String expectedSign = generateHiasoonSign(bizParams, timestamp, nonce, secretKey);
            if (sign == null || expectedSign == null) {
                return false;
            }
            return sign.equalsIgnoreCase(expectedSign);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 验证时间戳是否在有效期内（默认5分钟）
     *
     * @param timestamp 时间戳（毫秒）
     * @param maxAge    最大有效期（毫秒）
     * @return 是否在有效期内
     */
    public static boolean isTimestampValid(long timestamp, long maxAge) {
        long now = System.currentTimeMillis();
        return Math.abs(now - timestamp) <= maxAge;
    }

    /**
     * 验证时间戳是否在有效期内（默认5分钟 = 300000毫秒）
     */
    public static boolean isTimestampValid(long timestamp) {
        return isTimestampValid(timestamp, 300000L);
    }

    private static String hmacSha256Hex(String data, String key) throws NoSuchAlgorithmException, InvalidKeyException {
        javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
        javax.crypto.spec.SecretKeySpec secretKeySpec = new javax.crypto.spec.SecretKeySpec(key.getBytes(UTF8), mac.getAlgorithm());
        mac.init(secretKeySpec);
        byte[] hash = mac.doFinal(data.getBytes(UTF8));
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(b & 0xFF);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString().toLowerCase();
    }
}
