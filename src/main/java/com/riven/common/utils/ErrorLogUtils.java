package com.riven.common.utils;

public class ErrorLogUtils {

    public static String getErrorMsg(Exception e, int length) {
        if (e == null) {
            return "";
        }
        String message = e.getMessage();
        if (message == null) {
            return "";
        }
        return message.length() > length ? message.substring(0, length) : message;
    }
}
