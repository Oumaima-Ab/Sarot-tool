package com.oumaima.sarottool.util;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class HmacUtils {
    public static byte[] computeHmacSHA256(byte[] data, byte[] keyBytes) throws Exception {
        SecretKey key = new SecretKeySpec(keyBytes, "HmacSHA256");
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(key);
        return mac.doFinal(data);
    }

    public static boolean verifyHmacSHA256(byte[] data, byte[] expectedHmac, byte[] keyBytes) throws Exception {
        byte[] actualHmac = computeHmacSHA256(data, keyBytes);
        if (actualHmac.length != expectedHmac.length) return false;
        int result = 0;
        for (int i = 0; i < actualHmac.length; i++) {
            result |= actualHmac[i] ^ expectedHmac[i];
        }
        return result == 0;
    }
}