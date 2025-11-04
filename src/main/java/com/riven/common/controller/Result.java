package com.riven.common.controller;

import lombok.Data;

/**
 * 统一API响应结果封装
 *
 * @param <T> 数据类型
 */
@Data
public class Result<T> {
    private String code;
    private String msg;
    private T data;

    public Result() {
    }

    public Result(String code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static <T> Result<T> ofSuccess(T data) {
        return new Result<>("0", "success", data);
    }

    public static <T> Result<T> ofSuccess(String msg, T data) {
        return new Result<>("0", msg, data);
    }

    public static <T> Result<T> ofFail(String code, String msg) {
        return new Result<>(code, msg, null);
    }

    public static <T> Result<T> ofFail(String msg) {
        return new Result<>("-1", msg, null);
    }

    public boolean isSuccess() {
        return "0".equals(code);
    }
}
