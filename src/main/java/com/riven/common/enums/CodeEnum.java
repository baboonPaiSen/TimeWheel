package com.riven.common.enums;

public interface CodeEnum<T> {
    T getCode();


    /**
     * 根据code获取对应的枚举值
     *
     * @param enumClass 枚举类的Class对象
     * @param code 要匹配的code
     * @param <E> 枚举类型的泛型
     * @return 匹配到的枚举值，如果没有匹配到则返回null
     */
    public static <T, E extends CodeEnum<T>> E fromCode(Class<E> enumClass, T code) {
        for (E enumValue : enumClass.getEnumConstants()) {
            if (enumValue.getCode().equals(code)) {
                return enumValue;
            }
        }
        return null;
    }

    /**
     * 检查传入的字符串是否能匹配到枚举中的某个code
     *
     * @param enumClass 枚举类的Class对象
     * @param code 要检查的code
     * @param <E> 枚举类型的泛型
     * @return 如果匹配到返回true，否则返回false
     */
    public static <T, E extends CodeEnum<T>> boolean isValidCode(Class<E> enumClass, T code) {
        return fromCode(enumClass, code) != null;
    }
}