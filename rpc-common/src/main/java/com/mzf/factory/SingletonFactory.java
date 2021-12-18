package com.mzf.factory;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * 获取单例对象的工厂类
 *
 * @author 好大雨
 * @create 2021/9/17 21:30
 */
public final class SingletonFactory {
    private static final Map<String,Object> OBJECT_MAP = new ConcurrentHashMap<>();

    public SingletonFactory() {
    }

    /**
     *
     * @param c Class对象
     * @param <T>
     * @return 单例对象
     */
    public static <T> T getInstance(Class<T> c){
        if(c == null){
            throw new IllegalArgumentException();
        }

        String key = c.toString();
        if(OBJECT_MAP.containsKey(key)){
            return c.cast(OBJECT_MAP.get(key));
        }else{
            return c.cast(OBJECT_MAP.computeIfAbsent(key,k->{
                try{
                    return c.getDeclaredConstructor().newInstance();
                } catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                    throw new RuntimeException(e.getMessage(),e);
                }
            }));
        }
    }
}
