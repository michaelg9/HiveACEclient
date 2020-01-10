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

}
