package com.rabbitmq;

import com.rabbitmq.client.*;

public class ConnectionUtil {

    public static final String HOST_ADDRESS = "101.43.120.237";
    private static final ThreadLocal<Channel> threadLocalChannel = new ThreadLocal<>();

    // 共享的 Connection
    private static Connection connection;

    static {
        try {
            // 创建连接
            connection = createConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Connection createConnection() throws Exception {
        // 定义连接工厂
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(HOST_ADDRESS);
        factory.setPort(5672);
        factory.setVirtualHost("/");
        factory.setUsername("sqhh99");
        factory.setPassword("472540980");

        // 通过工厂获取连接
        return factory.newConnection();
    }

    // 获取连接
    public static Connection getConnection() {
        return connection;
    }

    // 获取线程局部的 Channel
    public static Channel getChannel() throws Exception {
        Channel channel = threadLocalChannel.get();

        if (channel == null) {
            // 如果线程没有自己的 channel，则创建一个新的
            channel = connection.createChannel();
            threadLocalChannel.set(channel);
        }

        return channel;
    }

    // 关闭 Channel 和 Connection
    public static void close() {
        try {
            Channel channel = threadLocalChannel.get();
            if (channel != null) {
                channel.close();
            }
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
