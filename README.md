# RabbitMqProject
基于rabbitmq实现的通讯软件

1、配置rabbitmq: 在ubuntu服务器上使用docker pull rabbitmq:management 拉取rabbitmq的镜像，拉取成功后创建docker-compose.yml脚本配置以下命令

 ![image](https://github.com/user-attachments/assets/5020c393-d4be-49f4-bf1e-c813834eaecc)

然后运行docker-compose.yml up -d即可

 ![image](https://github.com/user-attachments/assets/c960d5e7-6f85-4e47-88f6-36098ec5cec2)


2、使用java swing设计一个客户端界面：
客户端界面要确保可以输入rabbitmq的ip和端口进行连接，输入账号和密码进行登录和注册，使用JTextArea来输出信息，使用JList显示用户列表，创建用于发送消息和发送文件的组件，用于发送消息和文件

3、rabbitmq模块：
1）创建一个ConnectionUtil类，它用拥有两个静态方法，createConnection()用于创建rabbitmq的Connection，getChannel()用于返回rabbitmq的Channel对象
2）创建一个RabbitMQProducer类，它拥有三个静态方法，sendMessageToQueue(String queueName, Message msg)用于将消息直接发送到rabbitmq服务器，sendMessageToFanoutExchange(String exchangeName, Message msg)用于将消息发送到fanout类型的交换机，可以实现群发消息，sendMessageToTopicExchange(String exchangeName, String routingKey, Message msg)用于将消息通过路由键发送到topic类型的交换机，可以实现群发消息，也可以实现发送消息到指定队列
3）创建一个SerializationUtil类，它拥有两个静态方法，serialize(Object obj)用于将消息类型Message序列化为字节数组，deserialize(byte[] data, Class<T> clazz)用于将字节数组转换为Message对象。

4、client模块
1）创建一个SqClient类，继承自Thread,创建void send(Message msg)方法发送Message,重写run方法，用于监听rabbitmq队列。
2）创建一个Message类，封装type, sender, content, recipient，用于消息传递
3）创建User类，用于封装username、password
4）创建XmlHandler类，用于将User类的信息保存到.xml文档中

用户注册：
对应功能的代码截图：
 ![image](https://github.com/user-attachments/assets/6876e019-0edd-4504-81ea-8334df937f9e)

可以看到用户信息已保存到.xml文档里面了
 

用户登录：
测试结果：
 ![image](https://github.com/user-attachments/assets/5f1d84e6-7437-4c26-8b1d-ebd6f962f563)


聊天功能：
测试结果：

发送私人消息:
用户1212向用户123发送“你好”
 ![image](https://github.com/user-attachments/assets/09f2a692-14f9-4cb1-b0f1-d655190f4e5f)

发送公共消息：
用户sqhh99向所有用户发送“你好-----------------------------------------”
 ![image](https://github.com/user-attachments/assets/6a323a6c-0e88-46e3-8b54-88102890db5c)


文件传输：
测试结果：
在文件发送这一栏输入文件路径，或者点击按钮选择文件，并点击发送
 ![image](https://github.com/user-attachments/assets/2c5f2fa8-99b6-41cb-b024-8a947f33bbac)

用户1212向用户123发送opencv.txt,可以看到发送成功，并且用户123程序文件目录下出现了对应文件
 ![image](https://github.com/user-attachments/assets/32469940-a12c-4ecc-a872-422510d8aec5)




