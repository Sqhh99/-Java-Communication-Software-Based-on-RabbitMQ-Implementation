package com.rabbitmq;

import com.client.Message;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;

import java.nio.file.Files;
import java.nio.file.Paths;

public class RabbitMQProducer {

    /**
     * 简单队列模式：直接向指定队列发送消息。
     */
    public static void sendMessageToQueue(String queueName, Message msg) {
        try {
            Channel channel = ConnectionUtil.getChannel();
            channel.queueDeclare(queueName, true, false, false, null);
            byte[] body = SerializationUtil.serialize(msg);
            channel.basicPublish("", queueName, null, body);
            System.out.println("Message sent to queue [" + queueName + "]: " + msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 发布/订阅模式：发送消息到 Fanout 类型的 Exchange。
     */
    public static void sendMessageToFanoutExchange(String exchangeName, Message msg) {
        try {
            Channel channel = ConnectionUtil.getChannel();
            // 声明 Fanout 类型的交换机
            channel.exchangeDeclare(exchangeName, BuiltinExchangeType.FANOUT, true, false, false, null);
            byte[] body = SerializationUtil.serialize(msg);
            channel.basicPublish(exchangeName, "", null, body);
            System.out.println("Message sent to Fanout Exchange [" + exchangeName + "]: " + msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * 主题模式：发送消息到 Topic 类型的 Exchange，带主题路由键。
     */
    public static void sendMessageToTopicExchange(String exchangeName, String routingKey, Message msg) {
        try {
            Channel channel = ConnectionUtil.getChannel();
            // 声明 Topic 类型的交换机
            channel.exchangeDeclare(exchangeName, BuiltinExchangeType.TOPIC, true, false, false, null);
            byte[] body = SerializationUtil.serialize(msg);
            channel.basicPublish(exchangeName, routingKey, null, body);  // 具体的 routingKey
            System.out.println("Message sent to Topic Exchange [" + exchangeName + "] with Routing Key [" + routingKey + "]: " + msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
