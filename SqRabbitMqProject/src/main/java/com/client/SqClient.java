package com.client;

import com.rabbitmq.ConnectionUtil;
import com.rabbitmq.RabbitMQConsumer;
import com.rabbitmq.RabbitMQProducer;
import com.rabbitmq.client.*;
import com.rabbitmq.SerializationUtil;
import com.ui.ClientFrame;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

public class SqClient extends Thread {
    private Channel channel;
    private String queueName;
    private String username;
    private String fanoutExchangeName = "Sq_Fanout_exchange";
    private String topicExchangeName = "Sq_Topic_exchange";
    private boolean topicExchangeEnable = true;
    public SqClient(String username, List<String> userNames) {
        this.username = username;
        queueName = "Sq_" + username;
        try {
            channel = ConnectionUtil.getChannel();
            channel.exchangeDeclare(topicExchangeName, BuiltinExchangeType.TOPIC, true, false, false, null);
            channel.exchangeDeclare(fanoutExchangeName, BuiltinExchangeType.FANOUT, true, false, false, null);
            configExchange(userNames);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize RabbitMQ connection and channel.", e);
        }

    }
    public void configExchange(List<String> userNames) throws IOException {
        for (String name : userNames) {
            channel.queueDeclare("Sq_" + name, true, false, false, null);
            channel.queueBind("Sq_" + name,fanoutExchangeName,"");
            channel.queueBind("Sq_" + name,topicExchangeName,name+".msg");
            channel.queueBind("Sq_" + name,topicExchangeName,"*.msg");
        }
    }
    public void configExchange(String name) throws IOException {
        channel.queueDeclare("Sq_" + name, true, false, false, null);
        channel.queueBind("Sq_" + name,fanoutExchangeName,"");
        channel.queueBind("Sq_" + name,topicExchangeName,name+".msg");
        channel.queueBind("Sq_" + name,topicExchangeName,"*.msg");
    }

    @Override
    public void run() {
        try {
            channel.queueDeclare(queueName, true, false, false, null);
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                byte[] body = delivery.getBody();
                try {
                    // 反序列化消息
                    Message msg = SerializationUtil.deserialize(body, Message.class);
                    System.out.println(msg);
                    if (msg.type.equals("file")) {
                        String content = new String(msg.getFileContent(), StandardCharsets.UTF_8);
                        System.out.println(content);
                        String outputPath = msg.content;
                        Files.write(Paths.get(outputPath), content.getBytes(StandardCharsets.UTF_8));
                        ClientFrame.addNoticeTextArea("文件已成功接收: " + outputPath + "\n");
                    }
                    // 消息的接收条件
                    if (Objects.equals(msg.recipient, username) || Objects.equals(msg.type, "all")) {
                            ClientFrame.addNoticeTextArea(msg.sender + " > " + msg.recipient + ": " + msg.content + "\n");
                    }
                } catch (Exception e) {
                    e.printStackTrace(); // 更好的异常处理可以包括日志记录等
                }
            };
            // 设置自动确认
            channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {});
        } catch (Exception e) {
            e.printStackTrace();  // 更好的异常处理可以包括日志记录等
            // 可考虑在异常情况下加入重试机制
        }
    }
    public void send(Message msg) {
        if (msg.type.equals("text") || msg.type.equals("file")) {
            String send_queueName = "Sq_" + msg.recipient;
            if (topicExchangeEnable) {
                RabbitMQProducer.sendMessageToTopicExchange(topicExchangeName, msg.recipient+".msg", msg);
            } else {
                RabbitMQProducer.sendMessageToQueue(send_queueName, msg);
            }
        } else if (msg.type.equals("all") || msg.recipient.equals("all")) {
            if (topicExchangeEnable) {
                RabbitMQProducer.sendMessageToTopicExchange(topicExchangeName,"*.msg", msg);
            } else {
                RabbitMQProducer.sendMessageToFanoutExchange(fanoutExchangeName, msg);
            }
        }
    }

    // 优雅地停止线程
    public void stopClient() throws TimeoutException {
        try {
            if (channel != null) {
                channel.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
