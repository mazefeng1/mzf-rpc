package com.mzf.extension;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 *  扩展类加载器，用于加载用@SPI修饰的服务接口
 * @author 好大雨
 * @create 2021/9/20 0:18
 */
@Slf4j
public class ExtensionLoader<T>{
    private static final String SERVICE_DIRECTORY = "META-INF/extensions/";
    //缓存已有扩展点加载器
    private static final Map<Class<?>, ExtensionLoader<?>> EXTENSION_LOADERS = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Object> EXTENSION_INSTANCES = new ConcurrentHashMap<>();

    private final Class<?> type;
    private final Map<String, Holder<Object>> cachedInstances = new ConcurrentHashMap<>();
    private final Holder<Map<String, Class<?>>> cachedClasses = new Holder<>();

    private ExtensionLoader(Class<?> type) {
        this.type = type;
    }

    /**
     * 获取扩展类加载器
     * @param type
     * @param <S>
     * @return
     */
    public static <S> ExtensionLoader<S> getExtensionLoader(Class<S> type) {
        if (type == null) {
            throw new IllegalArgumentException("Extension type should not be null.");
        }
        if (!type.isInterface()) {
            throw new IllegalArgumentException("Extension type must be an interface.");
        }
        if (type.getAnnotation(SPI.class) == null) {
            throw new IllegalArgumentException("Extension type must be annotated by @SPI");
        }
        // 类加载器缓存map中取
        ExtensionLoader<S> extensionLoader = (ExtensionLoader<S>) EXTENSION_LOADERS.get(type);
        if (extensionLoader == null) {
            EXTENSION_LOADERS.putIfAbsent(type, new ExtensionLoader<S>(type));
            extensionLoader = (ExtensionLoader<S>) EXTENSION_LOADERS.get(type);
        }
        return extensionLoader;
    }

    /**
     * 根据name返回其一实例
     * @param name
     * @return instance
     */
    public T getExtension(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Extension name should not be null or empty.");
        }
        // firstly get from cache, if not hit, create one
        Holder<Object> holder = cachedInstances.get(name);
        if (holder == null) {
            cachedInstances.putIfAbsent(name, new Holder<>());
            holder = cachedInstances.get(name);
        }
        // create a singleton if no instance exists
        Object instance = holder.get();
        if (instance == null) {
            synchronized (holder) {
                instance = holder.get();
                if (instance == null) {
                    instance = createExtension(name);
                    holder.set(instance);
                }
            }
        }
        return (T) instance;
    }

    /**
     * name在map中值为null，从实例缓存map中取
     * @param name
     * @return
     */
    private T createExtension(String name) {
        // 获取扩展类，并取出key为name的值
        Class<?> clazz = getExtensionClasses().get(name);
        if (clazz == null) {
            throw new RuntimeException("No such extension of name " + name);
        }
        T instance = (T) EXTENSION_INSTANCES.get(clazz);
        //若无，需创建实例到map中
        if (instance == null) {
            try {
                EXTENSION_INSTANCES.putIfAbsent(clazz, clazz.newInstance());
                instance = (T) EXTENSION_INSTANCES.get(clazz);
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
        return instance;
    }

    /**
     * 获取所有扩展Class，只在初始化时做一次，如果cachedClass为null，调用createAdaptiveExtensionClass方法
     * @return
     */
    private Map<String, Class<?>> getExtensionClasses() {
        // 获取所有扩展Class
        Map<String, Class<?>> classes = cachedClasses.get();
        // double check
        if (classes == null) {
            synchronized (cachedClasses) {
                classes = cachedClasses.get();
                if (classes == null) {
                    classes = new HashMap<>();
                    // load all extensions from our extensions directory
                    loadDirectory(classes);
                    cachedClasses.set(classes);
                }
            }
        }
        return classes;
    }

    /**
     *  加载directory
     * @param extensionClasses
     */
    private void loadDirectory(Map<String, Class<?>> extensionClasses) {
        //"META-INF/extensions/"+类全限制类名
        String fileName = ExtensionLoader.SERVICE_DIRECTORY + type.getName();
        try {
            Enumeration<URL> urls;
            ClassLoader classLoader = ExtensionLoader.class.getClassLoader();
            urls = classLoader.getResources(fileName);
            if (urls != null) {
                while (urls.hasMoreElements()) {
                    URL resourceUrl = urls.nextElement();
                    loadResource(extensionClasses, classLoader, resourceUrl);
                }
            }
        } catch (IOException  e) {
            log.error(e.getMessage());
        }
    }

    /**
     * 加载SPI的Resource文件（key-value）
     * @param extensionClasses
     * @param classLoader
     * @param resourceUrl
     */
    private void loadResource(Map<String, Class<?>> extensionClasses, ClassLoader classLoader, URL resourceUrl) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resourceUrl.openStream(), UTF_8))) {
            String line;
            // read every line
            while ((line = reader.readLine()) != null) {
                // 注解
                final int ci = line.indexOf('#');
                if (ci >= 0) {
                    // 忽略注解
                    line = line.substring(0, ci);
                }
                line = line.trim();
                if (line.length() > 0) {
                    try {
                        final int ei = line.indexOf('=');
                        //标识名
                        String name = line.substring(0, ei).trim();
                        //全限定类名
                        String clazzName = line.substring(ei + 1).trim();
                        // key-value
                        if (name.length() > 0 && clazzName.length() > 0) {
                            Class<?> clazz = classLoader.loadClass(clazzName);
                            extensionClasses.put(name, clazz);
                        }
                    } catch (ClassNotFoundException e) {
                        log.error(e.getMessage());
                    }
                }

            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
