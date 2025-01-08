package com.socket;

import java.io.*;

public class SerializationUtil {
    // 将对象序列化为字节数组
    public static byte[] serialize(Object obj) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(obj);
            return bos.toByteArray();
        }
    }

    // 将字节数组反序列化为对象
    public static <T> T deserialize(byte[] data, Class<T> clazz) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(data);
             ObjectInputStream ois = new ObjectInputStream(bis)) {
            return clazz.cast(ois.readObject());
        }
    }
}
