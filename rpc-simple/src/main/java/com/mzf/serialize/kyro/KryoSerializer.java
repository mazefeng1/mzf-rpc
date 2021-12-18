package com.mzf.serialize.kyro;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import com.mzf.exception.SerializeException;
import com.mzf.remoting.dto.RpcRequest;
import com.mzf.remoting.dto.RpcResponse;
import com.mzf.serialize.Serializer;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 *
 * @author 好大雨
 * @create 2021/9/21 15:30
 */
@Slf4j
public class KryoSerializer implements Serializer {

    /**
     * Because Kryo is not thread safe. So, use ThreadLocal to store Kryo objects
     */
    private final ThreadLocal<Kryo> kryoThreadLocal = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
        //kryo.setReferences(true);//默认值为true,强调作用
        //循环引用，Kryo为了追求高性能，可以关闭循环引用的支持。
        //kryo.setRegistrationRequired(false);//默认值为false,强调作用
        //使用注册行为最大问题在于，其不保证同一个Class每一次注册的号码想用，这与注册的顺序有关，
        // 也就意味着在不同的机器、同一个机器重启前后都有可能拥有不同的编号，这会导致序列化产生问题，所以在分布式项目中，一般关闭注册行为
        //  本项目开启此功能
        kryo.register(RpcResponse.class);
        kryo.register(RpcRequest.class);
        return kryo;
    });

    /**
     *   序列化
     * @param obj 要序列化的对象
     * @return byte[]
     */
    @Override
    public byte[] serialize(Object obj) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             Output output = new Output(byteArrayOutputStream)) {
            Kryo kryo = kryoThreadLocal.get();
            // Object->byte:将对象序列化为byte数组
            kryo.writeObject(output, obj);
            kryoThreadLocal.remove();
            return output.toBytes();
        } catch (Exception e) {
            throw new SerializeException("Serialization failed");
        }
    }

    /**
     * 反序列化
     * @param bytes 序列化后的字节数组
     * @param clazz 目标类
     * @param <T>
     * @return
     */
    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
             Input input = new Input(byteArrayInputStream)) {
            Kryo kryo = kryoThreadLocal.get();
            // byte->Object:从byte数组中反序列化出对对象
            Object o = kryo.readObject(input, clazz);
            kryoThreadLocal.remove();
            return clazz.cast(o);
        } catch (Exception e) {
            throw new SerializeException("Deserialization failed");
        }
    }

}
