package cn.geekyspace.rabbitmq;

import cn.geekyspace.rabbitmq.utils.RabbitConstant;
import cn.geekyspace.rabbitmq.utils.RabbitUtils;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

public class Consumer {

    public static void main(String[] args) throws IOException, TimeoutException {
        // 用于创建MQ的物理连接
        Connection connection = RabbitUtils.getConnection();  // TCP connection（物理连接）
        Channel channel = connection.createChannel();         // AMQP channel（虚拟连接）

        // 声明一个队列，参数分别是：队列名称、是否持久化、是否排他、是否自动删除、其他参数
        channel.queueDeclare(RabbitConstant.QUEUE_HELLOWORLD, true, false, false, null);
        System.out.println(" [*] 等待消息。按CTRL+C退出");

        // 消费者回调
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println(" [x] 接收 '" + message + "'");
        };

        // 消费消息，参数分别是：队列名称、是否自动确认、消费者回调、取消回调
        channel.basicConsume(RabbitConstant.QUEUE_HELLOWORLD, true, deliverCallback, consumerTag -> {
        });
    }

}
