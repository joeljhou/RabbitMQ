# 高并发实战-RabbitMQ消息队列

## 1-认识RabbitMQ

**Message Queue（MQ）**

* ==消息队列（Message Queue）==，后文称**MQ**，是一种跨进程的通信机制，用于上下游传递消息。
* MQ作为消息中间件，最主要的作用系统之间的信息传递进行“**解耦**”，MQ是数据可靠性的重要保障。

**什么是RabbitMQ？**

[官网](https://www.rabbitmq.com/) | [教程](https://www.rabbitmq.com/tutorials)

* RabbitMQ是全世界最火的开源消息代理服务器，在全世界拥有超过35000个项目部署在RabbitMQ。
* RabbitMQ支持几乎所有的操作系统与编程语言。
* RabbitMQ提供了高并发、高可用的成熟方案，支持多种消息协议，易于部署与使用。

**RabbitMQ与其他MQ的对比**

| 特性        | RabbitMQ    | ActiveMQ | Kafka      | RocketMQ     |
|-----------|-------------|----------|------------|--------------|
| **社区活跃度** | 非常活跃        | 非常活跃     | 活跃         | 不活跃          |
| **持久化**   | 支持          | 支持       | 支持         | 支持           |
| **并发吞吐量** | 高           | 一般       | ==极高==     | ==极高==       |
| **数据可靠性** | ==极高==      | 一般       | 高          | 高            |
| **生态完整度** | 很好          | 很好       | 很好         | 一般           |
| **用户总量**  | 多           | 多->一般    | 较多         | 少            |
| **应该场景**  | 分布式、高可靠交易系统 | 传统业务系统   | 日志处理及大数据应用 | 互联网高并发、高可用应用 |

**RabbitMQ的应用场景**

* **解耦**：异构系统的数据传递
* **削峰填谷**：高并发程序的流量控制
* **订阅发布**：基于P2P，P2PPP的程序
* **TCC控制**：分布式系统的事务一致性TCC
* **数据可靠性**：高可靠性的交易系统

## 2-安装RabbitMQ

**RabbitMQ使用Erlang开发** | [版本对照](https://www.rabbitmq.com/which-erlang.html)

* Erlang(['ə:læŋ])是一种通用的面向并发的编程语言，Erlang是一个结构化，动态类型编程语言，内建并行计算支持。
* 使用Erlang来编写分布式应用要简单的多，Erlang运行时环境是一个虚拟机，有点像Java虚拟机，这样代码一经编译，同样可以随处运行。

[RabbitMQ 安装指南](https://www.rabbitmq.com/docs/download)

```shell
# latest RabbitMQ 3.13
docker run -it --name rabbitmq \
  -p 5672:5672 \
  -p 15672:15672 \
  rabbitmq:3.13-management
```

* 可视化控制台：[http://localhost:15672/](http://localhost:15672/)
* 管理员账户密码：guest / guest
* 5672 端口是RabbitMQ通信端口， 15672 是可视化控制台端口

![RabbitMQ登录后界面](http://img.geekyspace.cn/pictures/2024/202405091646342.png)

## 3-RabbitMQ常用命令

**进入 RabbitMQ 容器**

```shell
docker exec -it rabbitmq bash
```

这个命令将以交互模式进入 RabbitMQ 容器的终端，以便执行后续的 RabbitMQ 命令

**常用 RabbitMQ 命令**

1. **管理 RabbitMQ 服务**

   ```shell
   rabbitmq-server            # 前台启动
   rabbitmq-server -detached  # 后台启动
   rabbitmqctl stop           # 停止服务
   ```

2. **管理 RabbitMQ 应用程序**

   ```shell
   rabbitmqctl start_app      # 启动应用
   rabbitmqctl stop_app       # 停止应用
   ```

3. **查看 RabbitMQ 节点状态**

   ```shell
   rabbitmqctl status         # 查看状态
   ```

4. **插件管理**

    * `rabbitmq_management`—Web 管理插件

   ```shell
   rabbitmq-plugins list                        # 列出插件
   rabbitmq-plugins enable {pluginname}         # 启用插件
   rabbitmq-plugins disable {pluginname}        # 禁用插件
   ```

5. **用户管理**

   ```shell
   # 用户
   rabbitmqctl list_users                                 # 列出所有用户
   rabbitmqctl add_user {username} {password}             # 添加用户
   rabbitmqctl delete_user {username}                     # 删除用户
   
   # 权限
   rabbitmqctl change_password {username} {newpassword}               # 修改用户密码
   rabbitmqctl set_permissions -p {vhost} {username} ".*" ".*" ".*"   # 设置用户权限
   rabbitmqctl set_user_tags {username} {tag}                         # 设置用户角色
   ```

6. **队列管理**

   ```shell
   # 队列
   rabbitmqctl list_queues                  # 列出队列
   rabbitmqctl -p {vhost} purge_queue blue  # 清除队列
   
   # 虚拟主机
   rabbitmqctl list_vhost                          # 列出虚拟主机
   rabbitmqctl add_vhost {vhostpath}               # 创建虚拟主机
   rabbitmqctl list_permissions -p {vhostpath}     # 列出虚拟主机上所有权限
   rabbitmqctl delete——vhost {vhostpath}           # 删除虚拟主机
   ```

**RabbitMQ用户四种角色Tag**

| 用户角色Tag                | 描述                                                                         |
|------------------------|----------------------------------------------------------------------------|
| **超级管理员**(Admin)       | 可登陆管理控制台(启用management plugin的情况下)，可查看所有的信息，并且可以对用户，策略(policy)进行操作。         |
| **监控者**(Monitoring)    | 登陆管理控制台(启用management plugin的情况下)，同时可以查看rabbitmq节点的相关信息(进程数，内存使用情况，磁盘使用情况等) |
| **策略制定者**(Policymaker) | 可登陆管理控制台(启用management plugin的情况下), 同时可以对policy进行管理。但无法查看节点的相关信息。           |
| **普通管理者**(Management)  | 仅可登陆管理控制台(启用management plugin的情况下)，无法看到节点信息，也无法对策略进行管理。                    |

## 4-点对点MQ通信

**AMQP**

**AMQP**（Advanced Message Queuing
Protocol）是一种网络协议，用于在分布式系统中进行消息传递。它被设计用来支持高性能、可靠性和可扩展性的消息传递系统，常用于消息队列中间件（如
RabbitMQ）与应用程序之间的通信。

**基本概念**
![Hello World！](http://img.geekyspace.cn/pictures/2024/202405111147828.png)

* **Producer**：生产者，消息的提供者
* **Consumer**：消费者，消息的使用者
* **Message**：消息，程序间的通信的数据
* **Queue**：队列，消息存放的容器，消息先进先出
* **Vhost**：虚拟主机,相当于MQ的“数据库”，用于存储队列

**Java创建Maven项目使用RabbitMQ**

1. 新建一个`rabbitmq-quickstart`的Maven工程

2. 添加依赖 `amqp-client`

   ```pom
   <!-- RabbitMQ Java Client -->
   <!-- https://mvnrepository.com/artifact/com.rabbitmq/amqp-client -->
   <dependency>
     <groupId>com.rabbitmq</groupId>
     <artifactId>amqp-client</artifactId>
     <version>5.21.0</version>  <!-- 推荐使用最新版本 -->
   </dependency>
   ```

3. **创建 RabbitMQ 虚拟主机：**

   打开 RabbitMQ 的管理界面（通常在 `http://localhost:15672`），
   登录并进入虚拟主机管理页面。在这里创建一个名为 `/geekyspace`的虚拟主机。

4. **编写生产者和消费者代码：**

   ```java
   public class Producer {
   
       private final static String QUEUE_NAME = "helloworld";
   
       public static void main(String[] args) throws IOException, TimeoutException {
           // 用于创建MQ的物理连接
           ConnectionFactory factory = new ConnectionFactory();
           factory.setHost("localhost");
           factory.setPort(5672);
           factory.setUsername("zhouyu");
           factory.setPassword("123456");
           factory.setVirtualHost("/geekyspace");
   
           Connection connection = factory.newConnection();  // TCP connection（物理连接）
           Channel channel = connection.createChannel();     // AMQP channel（虚拟连接）
   
           // 声明一个队列，参数分别是：队列名称、是否持久化、是否排他、是否自动删除、其他参数
           channel.queueDeclare(QUEUE_NAME, true, false, false, null);
           System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
   
           // 消费者回调
           DeliverCallback deliverCallback = (consumerTag, delivery) -> {
               String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
               System.out.println(" [x] Received '" + message + "'");
           };
   
           // 消费消息，参数分别是：队列名称、是否自动确认、消费者回调、取消回调
           channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> {
           });
       }
   }
   ```

   ```java
   public class Consumer {
   
       private final static String QUEUE_NAME = "helloworld";
   
       public static void main(String[] args) throws IOException, TimeoutException {
           // 用于创建MQ的物理连接
           ConnectionFactory factory = new ConnectionFactory();
           factory.setHost("localhost");
           factory.setPort(5672);
           factory.setUsername("zhouyu");
           factory.setPassword("123456");
           factory.setVirtualHost("/geekyspace");
   
           try (Connection connection = factory.newConnection();  // TCP connection（物理连接）
                Channel channel = connection.createChannel()) {   // AMQP channel（虚拟连接）
   
               // 声明一个队列，参数分别是：队列名称、是否持久化、是否排他、是否自动删除、其他参数
               // 是否排他：只对首次声明它的连接可见，并在连接断开时自动删除
               channel.queueDeclare(QUEUE_NAME, true, false, false, null);
               String message = "Hello, RabbitMQ!";
   
               // 发送消息到队列，参数分别是：交换机名称、路由键、其他参数、消息内容
               // exchange：交换机名称，简单模式下为空字符串，表示使用默认交换机
               channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
   
               // 打印发送的消息
               System.out.println(" [x] Sent '" + message + "'");
           }
       }
   }
   ```

**代码运行结果：**

生产者：

```jshell
 [x] 发送 'Hello, RabbitMQ!'
```

消费者：

```shell
 [*] 等待消息。按CTRL+C退出
 [x] 接收 'Hello, RabbitMQ!'
```

## 5-封装工具类

**RabbitMQ消息状态**

* **Ready(就绪)**：消息已被送入队列，等待被消费
* **Unacked(未确认)**：消息已经被消费者认领，但还未被确认“消费成功”
* **Finished(完成)**：调用了ack方法，消息被确认“消费成功”

**RabbitMQ工具类**

```java
public class RabbitUtils {

    private static final ConnectionFactory connectionFactory = new ConnectionFactory();

    static {
        connectionFactory.setHost("localhost");
        connectionFactory.setPort(5672);         //5672是RabbitMQ的默认端口号
        connectionFactory.setUsername("zhouyu");
        connectionFactory.setPassword("123456");
        connectionFactory.setVirtualHost("/geekyspace");
    }

    public static Connection getConnection() {
        Connection conn = null;
        try {
            conn = connectionFactory.newConnection();
            return conn;
        } catch (Exception e) {
            // 运行时异常
            throw new RuntimeException(e);
        }
    }
}
```

**RabbitMQ常量类**

```java
public class RabbitConstant {

    public static final String QUEUE_HELLOWORLD = "helloworld";

}
```

基于工具类和常量类，重构生产者和消费者代码，简化代码逻辑。

## 6-RabbitMQ六种工作模式

[6种工作模式](https://www.rabbitmq.com/tutorials)

1. **简单模式（Simple Mode）**：也称为==点对点==模式（Point-to-Point），是最基本的工作模式。
   生产者将消息发送到队列，然后消费者从队列中接收并处理消息。
2. **工作队列模式（Work Queues Mode）**：也称为任务队列模式（Task Queues），多个消费者共享一个队列，
   每个消息只会被其中一个消费者处理。这种模式可以实现负载均衡和==任务分发==。
3. **发布/订阅模式（Publish/Subscribe Mode）**：发布者（生产者）将消息发送到交换机（Exchange），
   交换机将消息广播给与之绑定的所有队列，==每个队列可以有多个消费者==。
4. **路由模式（Routing Mode）**：在发布/订阅模式的基础上，引入了消息的路由规则。生产者将消息发送到指定的交换机，
   并指定消息的路由键（Routing Key），交换机根据==路由规则精准匹配==将消息发送到符合条件的队列。
5. **主题模式（Topics Mode）**：类似于路由模式，但是主题模式可以使用==通配符来模糊匹配==路由键。
   这样可以更灵活地定义路由规则，实现更精确的消息路由。
6. **RPC模式（Remote Procedure Call Mode）**：客户端通过发送请求消息到服务器端的队列，
   并等待服务器端的响应消息来实现远程过程调用。RPC模式可以在分布式系统中实现客户端与服务器端之间的通信。

![6种工作模式](http://img.geekyspace.cn/pictures/2024/202405140102903.png)

## 7-WorkQueue工作队列

> **工作队列模式**：也称为任务队列模式（Task Queues），多个消费者共享一个队列，每个消息只会被其中一个消费者处理。

**使用场景：**

12306订单系统 —> Rabbit MQ —> 短信服务1,短信服务2,短信服务3...

**编码实现：**

1. 发送者 OrderSystem
2. 接收者 SMSConsumer1,SMSConsumer2,SMSConsumer3

```java
/**
 * 工作队列模式适用于需要处理大量消息的场景，例如：订单系统中需要发送大量短信通知。
 */
public class OrderSystem {

    private static final Gson gson = new Gson();

    public static void main(String[] args) throws IOException, TimeoutException {
        // 创建连接和通道
        try (Connection connection = RabbitUtils.getConnection();
             Channel channel = connection.createChannel()) {

            // 声明队列
            channel.queueDeclare(RabbitConstant.QUEUE_SMS, true, false, false, null);

            // 发送100条消息
            for (int i = 1; i <= 100; i++) {
                SMS sms = new SMS("12306", randomPhoneNumber(), "您的车票已预订成功。订单号：" + i);
                String jsonMessage = gson.toJson(sms);
                channel.basicPublish("", RabbitConstant.QUEUE_SMS, null, jsonMessage.getBytes(StandardCharsets.UTF_8));
                System.out.println(" [x] Sent '" + jsonMessage + "'");
            }
            System.out.println("发送数据成功");
        }
    }

    // 生成随机手机号码
    private static String randomPhoneNumber() { ...}
}
```

```java
public class SMSService1 {
    public static void main(String[] args) throws IOException {
        // 创建连接和通道
        Connection connection = RabbitUtils.getConnection();
        Channel channel = connection.createChannel();

        // 声明队列
        channel.queueDeclare(RabbitConstant.QUEUE_SMS, true, false, false, null);

        // 注意⚠️：保证一次只分发一个，能者多劳
        channel.basicQos(1);

        // 消费者接收消息
        channel.basicConsume(RabbitConstant.QUEUE_SMS, false,
                (consumerTag, message) -> {
                    String jsonSMS = new String(message.getBody());
                    System.out.println("SMSService1-短信发送成功：" + jsonSMS);
                    // 手动ack确认
                    channel.basicAck(message.getEnvelope().getDeliveryTag(), false);
                },
                consumerTag -> {
                    // 取消消费回调
                });
    }
}
```

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SMS {
    private String name;
    private String mobile;
    private String content;

    // 自动生成 Getter、Setter、equals、hashCode 和 toString 方法

}
```

**代码运行结果：**

生产者：

```jshell
 [x] 发送 '{"name":"12306","mobile":"13187762586","content":"您的车票已预订成功。订单号：1"}'
 [x] 发送 '{"name":"12306","mobile":"13983064895","content":"您的车票已预订成功。订单号：2"}'
......
 [x] 发送 '{"name":"12306","mobile":"14726343208","content":"您的车票已预订成功。订单号：9"}'
```

消费者：

```shell
SMSService1-短信发送成功：{"name":"12306","mobile":"13187762586","content":"您的车票已预订成功。订单号：1"}
SMSService1-短信发送成功：{"name":"12306","mobile":"16444092808","content":"您的车票已预订成功。订单号：5"}
SMSService1-短信发送成功：{"name":"12306","mobile":"10926113620","content":"您的车票已预订成功。订单号：8"}
```

```shell
SMSService2-短信发送成功：{"name":"12306","mobile":"13983064895","content":"您的车票已预订成功。订单号：2"}
SMSService2-短信发送成功：{"name":"12306","mobile":"15863784238","content":"您的车票已预订成功。订单号：4"}
SMSService2-短信发送成功：{"name":"12306","mobile":"15749068610","content":"您的车票已预订成功。订单号：7"}
```

```shell
SMSService3-短信发送成功：{"name":"12306","mobile":"12200616646","content":"您的车票已预订成功。订单号：3"}
SMSService3-短信发送成功：{"name":"12306","mobile":"14014186823","content":"您的车票已预订成功。订单号：6"}
SMSService3-短信发送成功：{"name":"12306","mobile":"14726343208","content":"您的车票已预订成功。订单号：9"}
```

## 8-发布PUB-订阅SUB模式

> **发布/订阅模式**：发布者（生产者）将消息发送到交换机（Exchange），交换机将消息广播给与之绑定的所有队列，每个队列可以有多个消费者。

发布订阅模式中使用的交换机类型是`Fanout Exchange`。

交换机的类型有四种：

1. **Direct Exchange**：直连交换机，根据消息的路由键（Routing Key）将消息发送到指定的队列。
2. **Fanout Exchange**：扇形交换机，将消息广播到所有与之绑定的队列。
3. **Topic Exchange**：主题交换机，根据消息的路由键（Routing Key）模糊匹配将消息发送到符合条件的队列。
4. **Headers Exchange**：头交换机，根据消息的头部信息（Header）将消息发送到符合条件的队列。

**使用场景：**

发布订阅模式因为所有的订阅者都会收到相同的消息，所以适用于广播消息、通知等场景。

例如：中国气象局提供“天气预报”送入交换机，网易、新浪、搜狐等订阅者通过队列绑定该交换机，都可以收到“天气预报”消息。

**代码实现：**

1. 使用管理界面创建交换机`weather`，类型选择`fanout`。

   ![创建exchange](http://img.geekyspace.cn/pictures/2024/202405151611071.png)

2. 创建`WeatherBureau`发布者，发送天气预报消息。

   ```java
   /**
    * 发布-订阅模式适用于消息广播的场景，例如：天气预报发布天气信息，多个订阅者接收天气信息。
    */
   public class WeatherBureau {
       public static void main(String[] args) throws IOException, TimeoutException {
   
           try (Connection connection = RabbitUtils.getConnection();
                Channel channel = connection.createChannel()) {
   
               // 发布消息到交换机
               String message = "长沙天气：晴";
   
               // 注意⚠️：第一个参数是交换机名称，不再是默认的“”空字符串
               channel.basicPublish(RabbitConstant.EXCHANGE_WEATHER, "", null, message.getBytes());
   
               System.out.println(" [x] 发送 '" + message + "'");
           }
       }
   }
   ```

3. 创建`NetEase`、`Sina`、`Sohu`订阅者，接收天气预报消息。

   ```java
   public class NetEase {
       public static void main(String[] args) throws IOException {
           Connection connection = RabbitUtils.getConnection();
           Channel channel = connection.createChannel();
   
           // 注意⚠️：需要将队列绑定到交换机
           channel.queueDeclare(RabbitConstant.QUEUE_NETEASE, true, false, false, null);
           channel.queueBind(RabbitConstant.QUEUE_NETEASE, RabbitConstant.EXCHANGE_WEATHER, "");
           channel.basicQos(1);
           channel.basicConsume(RabbitConstant.QUEUE_NETEASE, false,
                   // 消费者接收消息回调
                   (consumerTag, message) -> {
                       String jsonSMS = new String(message.getBody());
                       System.out.println("网易新闻-收到消息：" + jsonSMS);
                       // 手动ack确认
                       channel.basicAck(message.getEnvelope().getDeliveryTag(), false);
                   },
                   // 消费者取消消费回调
                   consumerTag -> {
                   });
       }
   }
   ```

**代码运行结果：**

生产者：

```jshell
 [x] 发送 '长沙天气：晴'
```

消费者：

```shell
网易新闻-收到消息：长沙天气：晴
新浪-收到消息：长沙天气：晴
搜狐-收到消息：长沙天气：晴
```

## 9-路由Routing模式

> **路由模式**：在发布/订阅模式的基础上，引入了消息的路由规则。生产者将消息发送到指定的交换机，
> 并指定消息的路由键（Routing Key），交换机根据路由规则精准匹配将消息发送到符合条件的队列。

路由模式中使用的交换机类型是`Direct Exchange`。

**使用场景：**

路由模式适用于需要精确匹配消息的场景，例如：日志系统中根据日志级别将消息发送到不同的队列。

**代码实现：**

1. 使用管理界面创建交换机`logs`，类型选择`direct`。
2. 创建`LogSystem`发布者，发送日志消息。

   ```java
   /**
    * 路由模式适用于需要精确匹配消息的场景，例如：日志系统中根据日志级别将消息发送到不同的队列。
    */
   public class LogSystem {
       public static void main(String[] args) throws IOException, TimeoutException {
           try (Connection connection = RabbitUtils.getConnection();
                Channel channel = connection.createChannel()) {
   
               LinkedHashMap<String, String> logs = new LinkedHashMap<>();
               logs.put("error", "error message");
               logs.put("warning", "warning message");
               logs.put("info", "info message");
               logs.put("debug", "debug message");
   
               for (Map.Entry<String, String> entry : logs.entrySet()) {
                   String routingKey = entry.getKey();
                   String message = entry.getValue();
                   // 注意⚠️：第二个参数是 routingKey，用于消息的筛选
                   channel.basicPublish(RabbitConstant.EXCHANGE_LOGS, routingKey, null, message.getBytes());
                   System.out.println(" [x] 发送 '" + routingKey + "':'" + message + "'");
               }
           }
       }
   }
   ```

3. 创建`ErrorConsumer`、`InfoConsumer`、`WarningConsumer`消费者，接收日志消息。

   ```java
   public class InfoConsumer {
       public static void main(String[] args) throws IOException {
           Connection connection = RabbitUtils.getConnection();
           Channel channel = connection.createChannel();
   
           // 注意⚠️：队列绑定交换机时，需要指定routingKey进行规则匹配
           channel.queueDeclare(RabbitConstant.QUEUE_INFO, true, false, false, null);
           channel.queueBind(RabbitConstant.QUEUE_INFO, RabbitConstant.EXCHANGE_LOGS, "info");
           channel.queueBind(RabbitConstant.QUEUE_INFO, RabbitConstant.EXCHANGE_LOGS, "debug");
   
           channel.basicQos(1);
           channel.basicConsume(RabbitConstant.QUEUE_INFO, false,
                   // 消费者接收消息回调
                   (consumerTag, message) -> {
                       String jsonSMS = new String(message.getBody());
                       System.out.println("InfoConsumer-收到消息：" + jsonSMS);
                       // 手动ack确认
                       channel.basicAck(message.getEnvelope().getDeliveryTag(), false);
                   },
                   // 消费者取消消费回调
                   consumerTag -> {
                   });
       }
   }
   ```

**代码运行结果：**

生产者：

```jshell
 [x] 发送 'error':'error message'
 [x] 发送 'warning':'warning message'
 [x] 发送 'info':'info message'
 [x] 发送 'debug':'debug message'
```

消费者：

```shell
InfoConsumer-收到消息：info message
InfoConsumer-收到消息：debug message
```

```shell
WarningConsumer-收到消息：warning message
```

```shell
ErrorConsumer-收到消息：error message
```

## 10-主题Topics模式

> **主题模式**：类似于路由模式，但是主题模式可以使用通配符来模糊匹配路由键。这样可以更灵活地定义路由规则，实现更精确的消息路由。

主题模式中使用的交换机类型是`Topic Exchange`。

模糊匹配规则：

* `*`：匹配一个单词
* `#`：匹配零个或多个单词
* `.`：分隔单词

**使用场景：**

主题模式适用于需要更灵活的消息路由规则的场景，例如：新闻系统中根据新闻类型将消息发送到不同的队列。

**代码实现：**

1. 使用管理界面创建交换机`news`，类型选择`topic`。
2. 创建`NewsSystem`发布者，发送新闻消息。

   ```java
   /**
    * 主题模式适用于消息筛选的场景，例如：新闻系统发布新闻信息，订阅者根据关键字接收新闻信息。
    */
   public class NewsSystem {
       public static void main(String[] args) throws IOException, TimeoutException {
           try (Connection connection = RabbitUtils.getConnection();
                Channel channel = connection.createChannel()) {
   
               LinkedHashMap<String, String> news = new LinkedHashMap<>();
               news.put("china.news", "中国新闻");
               news.put("china.weather", "中国天气");
               news.put("world.news", "国际新闻");
               news.put("world.weather", "国际天气");
   
               for (Map.Entry<String, String> entry : news.entrySet()) {
                   String routingKey = entry.getKey();
                   String message = entry.getValue();
                   // 注意⚠️：第二个参数是 routingKey，用于消息的筛选
                   channel.basicPublish(RabbitConstant.EXCHANGE_NEWS, routingKey, null, message.getBytes());
                   System.out.println(" [x] 发送 '" + routingKey + "':'" + message + "'");
               }
           }
       }
   }
   ```

3. 创建`ChinaNewsConsumer`、`WorldNewsConsumer`消费者，接收新闻消息。

   ```java
   public class ChinaNewsConsumer {
       public static void main(String[] args) throws IOException {
           Connection connection = RabbitUtils.getConnection();
           Channel channel = connection.createChannel();
   
           // 注意⚠️：队列绑定交换机时，需要指定routingKey进行规则匹配
           channel.queueDeclare(RabbitConstant.QUEUE_CHINA_NEWS, true, false, false, null);
           channel.queueBind(RabbitConstant.QUEUE_CHINA_NEWS, RabbitConstant.EXCHANGE_NEWS, "china.*");
   
           channel.basicQos(1);
           channel.basicConsume(RabbitConstant.QUEUE_CHINA_NEWS, false,
                   // 消费者接收消息回调
                   (consumerTag, message) -> {
                       String jsonSMS = new String(message.getBody());
                       System.out.println("ChinaNewsConsumer-收到消息：" + jsonSMS);
                       // 手动ack确认
                       channel.basicAck(message.getEnvelope().getDeliveryTag(), false);
                   },
                   // 消费者取消消费回调
                   consumerTag -> {
                   });
       }
   }
   ```

**代码运行结果：**

生产者：

```jshell
 [x] 发送 'china.news':'中国新闻'
 [x] 发送 'china.weather':'中国天气'
 [x] 发送 'world.news':'国际新闻'
 [x] 发送 'world.weather':'国际天气'
```

消费者：

```shell
ChinaNewsConsumer-收到消息：中国新闻
ChinaNewsConsumer-收到消息：中国天气
```

```shell
WorldNewsConsumer-收到消息：国际新闻
WorldNewsConsumer-收到消息：国际天气
```

## 11-RabbitMQ消息确认机制

**消息确认机制**：

RabbitMQ在投递消息的过程中充当代理人（Broker），生产者将消息发送到RabbitMQ，RabbitMQ将消息投递给消费者。

**消息确认涉及两种状态**：

* **Confirm**：生产者将消息发送到Broker时的状态，后续会出现两种情况：
    * `ack`：Broker成功接收到消息
    * `nack`：Broker拒收消息。原因有多种，例如：队列已满、消息格式错误，限流，IO异常等。
* **Return**：Broker正常接收（ack）后，但Broker没有对应的队列进行投递时产生的状态，消息被退回给生产者。

注意⚠️：以上两种状态是Broker与生产者之间的状态，与消费者无关。

**使用场景**：

对于一些关键业务的消息传递，如金融订单支付，需要保证消息的可靠性传递，此时需要使用消息确认机制。

**代码实现：**

1. 使用管理界面创建交换机`payment`，类型选择`topic`。
2. 创建`PaymentSystem`发布者，发送支付消息。

   ```java
   /**
    * 消息确认机制适用于需要保证消息可靠性传递的场景，例如：金融系统中支付订单。
    */
   public class PaymentSystem {
       public static void main(String[] args) throws IOException, TimeoutException {
   
           //注意⚠️：关闭连接就无法监听回掉
           Connection connection = RabbitUtils.getConnection();
           Channel channel = connection.createChannel();
   
           // 开启confirm监听模式
           channel.confirmSelect();
   
           // 添加消息确认监听器
           channel.addConfirmListener(
                   // ackCallback
                   (deliveryTag, multiple) -> {
                       System.out.println("订单已被Broker接收，投递标签：" + deliveryTag);
                   },
                   // nackCallback
                   (deliveryTag, multiple) -> {
                       System.out.println("订单已被Broker拒收，投递标签：" + deliveryTag);
                   });
   
           // 添加消息退回监听器
           channel.addReturnListener(returnMessage -> {
               System.out.println("========支付订单被退回========");
               System.out.println("退回编码：" + returnMessage.getReplyCode() + "，退回描述：" + returnMessage.getReplyText());
               System.out.println("交换机：" + returnMessage.getExchange() + "，路由键：" + returnMessage.getRoutingKey());
               System.out.println("退回主题：" + new String(returnMessage.getBody()));
               System.out.println("===========================");
           });
   
           // 发送支付订单消息
           LinkedHashMap<String, String> paymentOrder = new LinkedHashMap<>();
           paymentOrder.put("alipay.20991011", "支付宝订单20991011");
           paymentOrder.put("wechat.20991011", "微信订单20991011");
           paymentOrder.put("unionpay.20991011", "银联订单20991011");
   
           for (Map.Entry<String, String> entry : paymentOrder.entrySet()) {
               String routingKey = entry.getKey();
               String message = entry.getValue();
               // 注意⚠️：第三个参数是 mandatory，用于消息的退回
               // 当为 true  时，如果消息无法正常投递则 return 回生产者；
               // 当为 false 时，直接将消息放弃；
               channel.basicPublish(RabbitConstant.EXCHANGE_PAYMENT, routingKey, true, null, message.getBytes());
               System.out.println(" [x] 发送 '" + routingKey + "':'" + message + "'");
           }
   
       }
   }
   ```

3. 创建`AlipayConsumer`、`WechatConsumer`消费者，接收支付消息。

   ```java
   public class AlipayPaymentConsumer {
       public static void main(String[] args) throws IOException, TimeoutException {
           Connection connection = RabbitUtils.getConnection();
           Channel channel = connection.createChannel();
   
           channel.queueDeclare(RabbitConstant.QUEUE_ALIPAY, true, false, false, null);
           channel.queueBind(RabbitConstant.QUEUE_ALIPAY, RabbitConstant.EXCHANGE_PAYMENT, "alipay.*");
   
           channel.basicConsume(RabbitConstant.QUEUE_ALIPAY, false, (consumerTag, message) -> {
               System.out.println("支付宝收到订单：" + new String(message.getBody()));
               // 手动ack确认
               channel.basicAck(message.getEnvelope().getDeliveryTag(), false);
           }, consumerTag -> {
           });
       }
   }
   ```

**代码运行结果：**

生产者：

```jshell
 [x] 发送 'alipay.20991011':'支付宝订单20991011'
 [x] 发送 'wechat.20991011':'微信订单20991011'
 [x] 发送 'unionpay.20991011':'银联订单20991011'
========支付订单被退回========
退回编码：312，退回描述：NO_ROUTE
交换机：payment，路由键：unionpay.20991011
退回主题：银联订单20991011
===========================
订单已被Broker接收，投递标签：1
订单已被Broker接收，投递标签：3
订单已被Broker接收，投递标签：2
```

消费者：

```shell
支付宝收到订单：支付宝订单20991011
微信收到订单：微信订单20991011
```

## 12-Spring整合RabbitMQ

有了以上的基础知识，我们可以使用Spring整合RabbitMQ，实现更加便捷的消息传递。

1.创建一个`spring-rabbitmq`的Maven项目。

2.添加依赖`spring-rabbit`。

3.编写配置文件`applicationContext.xml`，使用`application.properties`进行配置。

```xml
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:p="http://www.springframework.org/schema/p"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:rabbit="http://www.springframework.org/schema/rabbit"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
           http://www.springframework.org/schema/beans/spring-beans.xsd
           http://www.springframework.org/schema/context
           http://www.springframework.org/schema/context/spring-context.xsd
           http://www.springframework.org/schema/rabbit
           http://www.springframework.org/schema/rabbit/spring-rabbit.xsd">

    <!-- 加载外部属性文件 -->
    <!-- property-placeholder只能加载properties文件，不能加载yaml文件 -->
    <context:property-placeholder location="classpath:application.properties"/>

    <!-- RabbitMQ连接工厂 -->
    <rabbit:connection-factory id="connectionFactory"
                               host="${spring.rabbitmq.host}"
                               port="${spring.rabbitmq.port}"
                               username="${spring.rabbitmq.username}"
                               password="${spring.rabbitmq.password}"
                               virtual-host="${spring.rabbitmq.virtual-host}"/>

    <!-- 声明一个名为topicExchange的交换机，自动创建，类型为topic -->
    <!-- 交换机类型有四种：direct、fanout、topic、headers -->
    <rabbit:topic-exchange name="topicExchange" auto-declare="true">
        <!-- 绑定队列，pattern表示匹配规则 -->
        <rabbit:bindings>
            <rabbit:binding queue="topicQueue" pattern="china.*"/>
            <rabbit:binding queue="topicQueue" pattern="us.*"/>
        </rabbit:bindings>
    </rabbit:topic-exchange>

    <!-- 创建队列 -->
    <rabbit:queue name="topicQueue" auto-declare="true"
                  durable="true" exclusive="false" auto-delete="false"/>

    <!-- RabbitMQ模板 -->
    <rabbit:template id="rabbitTemplate" connection-factory="connectionFactory" exchange="topicExchange"/>

    <!-- 消息生产者 -->
    <bean id="newsProducer" class="cn.geekyspace.rabbitmq.exchange.NewsProducer"
          p:rabbitTemplate-ref="rabbitTemplate"/>

    <!-- 消息消费者 -->
    <bean id="newsConsumer" class="cn.geekyspace.rabbitmq.consumer.NewsConsumer"
          p:rabbitTemplate-ref="rabbitTemplate"/>

    <!-- RabbitAdmin对象用于创建，删除，绑定队列，交换机等 -->
    <rabbit:admin id="rabbitAdmin" connection-factory="connectionFactory"/>

</beans>
```

```properties
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=zhouyu
spring.rabbitmq.password=123456
spring.rabbitmq.virtual-host=/geekyspace
```

4.创建`NewsProducer`发布者，发送新闻消息。

```java
/**
 * 新闻生产者，生产者针对交换机发送消息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewsProducer {

    private RabbitTemplate rabbitTemplate;
    private static final Gson gson = new Gson();

    // 发布新闻
    public void sendNews(String routingKey, News news) {
        rabbitTemplate.convertAndSend(routingKey, gson.toJson(news));
        System.out.println("新闻发送成功，标题: " + news.getTitle());
    }

    public static void main(String[] args) {
        // 初始化IOC容器
        ApplicationContext ctx = new ClassPathXmlApplicationContext("classpath:applicationContext.xml");
        NewsProducer np = (NewsProducer) ctx.getBean("newsProducer");

        // 发布新闻
        np.sendNews("us.20240513", new News("新华社", "GPT-4o简介", "GPT-4o立即试用", new Date()));
        np.sendNews("china.20240516", new News("36氪", "Kimi.ai", "帮你看更大的世界", new Date()));
    }

}
```

5.创建`NewsConsumer`消费者，接收新闻消息。

```java
/**
 * 新闻消费者，消费者从队列中接收消息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewsConsumer implements MessageListener {

    private RabbitTemplate rabbitTemplate;
    private static final Gson gson = new Gson();

    @Override
    public void onMessage(Message message) {
        // 处理接收到的消息
        final News news = gson.fromJson(new String(message.getBody()), News.class);
        System.out.printf("接收到最新新闻: 标题-%s 内容-%s%n", news.getTitle(), news.getContent());
    }

    public static void main(String[] args) {
        //初始化IOC容器
        ApplicationContext ctx = new ClassPathXmlApplicationContext("classpath:applicationContext.xml");
        RabbitTemplate rabbitTemplate = ctx.getBean(RabbitTemplate.class);

        // 创建消费者
        NewsConsumer newsConsumer = new NewsConsumer();

        // 设置消息监听容器
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(rabbitTemplate.getConnectionFactory());
        container.setQueueNames("topicQueue"); // 设置要监听的队列名
        container.setMessageListener(newsConsumer);

        // 启动监听
        container.start();
    }
}
```

## 13-使用RabbitAdmin管理MQ

**RabbitAdmin** 是 RabbitMQ 的管理组件，用于管理 RabbitMQ 的交换机、队列、绑定关系等。

**代码示例：**

```java
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext.xml")
public class RabbitAdminTest {

    @Autowired
    private RabbitAdmin rabbitAdmin;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    // 创建交换机
    @Test
    public void testCreateExchange() {
        rabbitAdmin.declareExchange(new DirectExchange("test.direct", true, false));
        rabbitAdmin.declareExchange(new FanoutExchange("test.fanout", true, false));
        rabbitAdmin.declareExchange(new TopicExchange("test.topic", true, false));
    }

    // 删除交换机
    @Test
    public void testDeleteExchange() {
        rabbitAdmin.deleteExchange("test.direct");
        rabbitAdmin.deleteExchange("test.fanout");
        rabbitAdmin.deleteExchange("test.topic");
    }

    // 创建队列
    @Test
    public void testCreateQueue() {
        rabbitAdmin.declareQueue(new Queue("test.direct.queue", true));
        rabbitAdmin.declareQueue(new Queue("test.fanout.queue", true));
        rabbitAdmin.declareQueue(new Queue("test.topic.queue", true));
    }

    // 删除队列
    @Test
    public void testDeleteQueue() {
        rabbitAdmin.deleteQueue("test.direct.queue");
        rabbitAdmin.deleteQueue("test.topic.queue");
        rabbitAdmin.deleteQueue("test.fanout.queue");
    }

    // 绑定队列
    @Test
    public void testBinding() {
        Binding directBinding = new Binding(
                "test.direct.queue", Binding.DestinationType.QUEUE,
                "test.direct", "test.direct.queue", null);
        Binding fanoutBinding = new Binding(
                "test.fanout.queue", Binding.DestinationType.QUEUE,
                "test.fanout", "#", null);
        Binding topicBinding = new Binding(
                "test.topic.queue", Binding.DestinationType.QUEUE,
                "test.topic", "#", null);
        rabbitAdmin.declareBinding(directBinding);
        rabbitAdmin.declareBinding(fanoutBinding);
        rabbitAdmin.declareBinding(topicBinding);
    }

    // 发送消息
    @Test
    public void testSendMessage() {
        // 直连交换机，用于简单模式和工作队列
        rabbitTemplate.convertAndSend("test.direct", "test.direct.queue", "Hello, RabbitMQ !");
        // 扇形交换机，用于发布订阅
        rabbitTemplate.convertAndSend("test.fanout", "", "长沙天气：晴");
        // 主题交换机，用于路由模式和主题模式
        rabbitTemplate.convertAndSend("test.topic", "china.news", "中国新闻");

        // q: Headers交换机工作原理是什么？
        // a: 通过消息头来路由消息，通过 x-match 参数来指定匹配规则，有 all 和 any 两种规则。
    }

    // 接收消息
    @Test
    public void testReceiveMessage() {
        Object directMessage = rabbitTemplate.receiveAndConvert("test.direct.queue");
        Object fanoutMessage = rabbitTemplate.receiveAndConvert("test.fanout.queue");
        Object topicMessage = rabbitTemplate.receiveAndConvert("test.topic.queue");
        System.out.println("directMessage = " + directMessage);
        System.out.println("fanoutMessage = " + fanoutMessage);
        System.out.println("topicMessage = " + topicMessage);
    }

}
```

期间提了一个[Issues-11268](https://github.com/rabbitmq/rabbitmq-server/discussions/11268)，关于直连交换机路由键为`#`的问题。

## 14-使用SpringBoot整合RabbitMQ

Java开发者最常用的框架之一是SpringBoot，SpringBoot提供了丰富的自动配置功能，可以简化RabbitMQ的配置。

1. 创建一个`springboot-rabbitmq`的SpringBoot项目。
2. 添加依赖`spring-boot-starter-amqp`。
3. 编写配置文件`application.yml`。

```yaml
spring:
  application:
    name: springboot-rabbitmq
  rabbitmq:
    # 连接配置
    host: localhost
    port: 5672
    username: zhouyu
    password: 123456
    virtual-host: /geekyspace
    connection-timeout: 1000
    # 生产者配置：
    publisher-confirm-type: correlated  # 对于 Spring Boot 2.2+，替代了 publisher-confirms 和 publisher-returns
    template:
      mandatory: true
    # 消费者配置：
    listener:
      simple:
        acknowledge-mode: manual
        concurrency: 1
        max-concurrency: 5
```

4.使用管理界面创建交换机`springboot-exchange`，类型选择`topic`，并创建一个队列`springboot-queue`与之绑定。

*
缺少交换机报错：`reply-code=404, reply-text=NOT_FOUND - no exchange 'springboot-exchange' in vhost '/geekyspace', class-id=60, method-id=40`

* 缺少绑定的队列报错：` reply-code=312, reply-text=NO_ROUTE`

5.编写生产者`MessageProducer`及员工类`Employee`。

```java
@Component
@RequiredArgsConstructor
public class MessageProducer {

    // 构造函数注入
    private final RabbitTemplate rabbitTemplate;

    private static final Gson gson = new Gson();

    RabbitTemplate.ConfirmCallback confirmCallback = (correlationData, ack, cause) -> {
        System.out.println("消息id:" + correlationData);
        System.out.println("ack:" + ack);
        if (ack) {
            System.out.println("消息发送确认成功");
        } else {
            System.out.println("消息发送确认失败:" + cause);
        }
    };

    RabbitTemplate.ReturnsCallback returnCallback = returnedMessage -> {
        System.out.println("========发送失败回掉========");
        System.out.println("退回编码: " + returnedMessage.getReplyCode() + ", 退回描述: " + returnedMessage.getReplyText());
        System.out.println("交换机: " + returnedMessage.getExchange() + ", 路由键：" + returnedMessage.getRoutingKey());
        System.out.println("消息主体: " + new String(returnedMessage.getMessage().getBody()));
        System.out.println("===========================");
    };

    // 生产者发送消息
    public void sendMessages(Employee employee) {

        // 消息发送确认，确认消息是否到达broker服务器
        rabbitTemplate.setConfirmCallback(confirmCallback);

        // 消息发送失败返回到队列中
        // 必须配置 spring.rabbitmq.template.mandatory=true 才能使用
        rabbitTemplate.setReturnsCallback(returnCallback);

        // 消息的附加信息，即自定义id
        final CorrelationData cd = new CorrelationData(employee.getNumber() + "-" + System.currentTimeMillis());
        rabbitTemplate.convertAndSend("springboot-exchange", "hr.employee", gson.toJson(employee), cd);
    }
}
```

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Employee implements Serializable {

    // 员工编号
    private String number;

    // 员工姓名
    private String name;

    // 员工年龄
    private Integer age;

}
```

6.编写消费者`MessageConsumer`。

```java
@Component
public class MessageConsumer {

    private static final Gson gson = new Gson();

    /**
     * 消费者监听消息，并处理接收到的消息
     */
    // @RabbitHandler注解，标识该方法是 RabbitMQ 的消息处理方法
    @RabbitHandler
    // @RabbitListener注解，标识该方法是 RabbitMQ 的消息监听器
    @RabbitListener(bindings = {
            // 绑定到指定的队列，从指定的交换机接收消息，使用指定的路由键进行绑定。
            @QueueBinding(
                    value = @Queue(value = "springboot-queue", declare = "true"),
                    exchange = @Exchange(value = "springboot-exchange", declare = "true", type = "topic"),
                    key = "#")
    })
    // 可以使用@Payload注解，标识该方法的参数是消息体
    public void receiveMessages(@Payload String message, Channel channel,
                                @Headers Map<String, Object> headers) {
        System.out.println("===========================");
        Employee employee = gson.fromJson(message, Employee.class);
        System.out.println("接收到消息：员工编号：" + employee.getNumber()
                + "，员工姓名：" + employee.getName()
                + "，员工年龄：" + employee.getAge());
        try {
            // 手动ack确认
            channel.basicAck((Long) headers.get(AmqpHeaders.DELIVERY_TAG), false);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("===========================");
    }

}
```

7.编写测试类`SpringbootRabbitmqApplicationTests`，用于测试消息发送。

```java
@SpringBootTest
class SpringbootApplicationTests {

    @Autowired
    private MessageProducer messageProducer;

    @Test
    void testSendMsg() {
        messageProducer.sendMessages(new Employee("1001", "张三", 25));
    }

}
```

8. 启动项目，在控制台查看日志，观察消息和接收情况。

## 15-RabbitMQ集群架构模式

