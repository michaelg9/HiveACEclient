package com.ace.mqtt.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Base64;

public class StringUtils {

    public static boolean isEmpty(@Nullable final String text) {
        return text == null || "".equals(text);
    }

    @NotNull
    public static String bytesToBase64(@NotNull final byte[] buf) {
        return Base64.getEncoder().encodeToString(buf);
    }

    public static byte[] combineByteArrays(@NotNull final byte[] a, @NotNull final byte[] b) {
        final byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

}
