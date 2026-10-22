
package com.hiasoon.sdk.sign;

import java.util.Random;

public class NonceGenerator {
    private static final String CHARSET = "ABCDEFGHJKMNPQRSTWXYZabcdefhijkmnprstwxyz2345678";
    private static final Random RANDOM = new Random();

    public static String generateNonce() {
        return generateNonce(6);
    }

    public static String generateNonce(int length) {
        if (length <= 0) {
            length = 6;
        }
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARSET.charAt(RANDOM.nextInt(CHARSET.length())));
        }
        return sb.toString();
    }
}
