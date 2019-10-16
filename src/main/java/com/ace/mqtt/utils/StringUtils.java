package com.ace.mqtt.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StringUtils {

    @NotNull
    public static boolean isEmpty(@Nullable String text) {
        return text == null || "".equals(text);
    }
}
