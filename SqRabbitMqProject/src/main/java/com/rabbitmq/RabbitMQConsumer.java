package com.rabbitmq;

import com.rabbitmq.client.*;

public class RabbitMQConsumer {
    public static void consumeMessage(String queueName, Channel channel, DeliverCallback deliverCallback) {
        try {
            // 声明队列
            channel.queueDeclare(queueName, true, false, false, null);

            // 启动消费者
            channel.basicConsume(queueName, false, deliverCallback, consumerTag -> {
                System.out.println("Consumer cancelled: " + consumerTag);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

