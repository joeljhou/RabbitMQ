package cn.geekyspace.rabbitmq;

import cn.geekyspace.rabbitmq.utils.RabbitConstant;
import cn.geekyspace.rabbitmq.utils.RabbitUtils;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class Producer {

    public static void main(String[] args) throws IOException, TimeoutException {
        // 用于创建MQ的物理连接
        try (Connection connection = RabbitUtils.getConnection();  // TCP connection（物理连接）
             Channel channel = connection.createChannel()) {   // AMQP channel（虚拟连接）

            // 声明一个队列，参数分别是：队列名称、是否持久化、是否排他、是否自动删除、其他参数
            // 是否排他：只对首次声明它的连接可见，并在连接断开时自动删除
            channel.queueDeclare(RabbitConstant.QUEUE_HELLOWORLD, true, false, false, null);
            String message = "Hello, RabbitMQ!";

            // 发送消息到队列，参数分别是：交换机名称、路由键、其他参数、消息内容
            // exchange：交换机名称，简单模式下为空字符串，表示使用默认交换机
            channel.basicPublish("", RabbitConstant.QUEUE_HELLOWORLD, null, message.getBytes());

            // 打印发送的消息
            System.out.println(" [x] 发送 '" + message + "'");
        }
    }
}
