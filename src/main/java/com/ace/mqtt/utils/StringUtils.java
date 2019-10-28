package com.ace.mqtt.utils;

import org.jetbrains.annotations.Nullable;

public class StringUtils {

    public static boolean isEmpty(@Nullable final String text) {
        return text == null || "".equals(text);
    }
}
