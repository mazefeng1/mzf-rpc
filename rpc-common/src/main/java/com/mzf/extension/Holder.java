package com.mzf.extension;

/**
 * @author 好大雨
 * @create 2021/9/20 0:19
 */
public class Holder<T> {
    private volatile T value;

    public T get() {
        return value;
    }

    public void set(T value) {
        this.value = value;
    }
}
