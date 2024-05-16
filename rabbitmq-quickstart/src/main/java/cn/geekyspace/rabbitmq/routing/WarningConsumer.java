package cn.geekyspace.rabbitmq.routing;

import cn.geekyspace.rabbitmq.utils.RabbitConstant;
import cn.geekyspace.rabbitmq.utils.RabbitUtils;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import java.io.IOException;

public class WarningConsumer {
    public static void main(String[] args) throws IOException {
        Connection connection = RabbitUtils.getConnection();
        Channel channel = connection.createChannel();

        // 注意⚠️：队列绑定交换机时，需要指定routingKey进行规则匹配
        channel.queueDeclare(RabbitConstant.QUEUE_WARNING, true, false, false, null);
        channel.queueBind(RabbitConstant.QUEUE_WARNING, RabbitConstant.EXCHANGE_LOGS, "warning");

        channel.basicQos(1);
        channel.basicConsume(RabbitConstant.QUEUE_WARNING, false,
                (consumerTag, message) -> {
                    String jsonSMS = new String(message.getBody());
                    System.out.println("WarningConsumer-收到消息：" + jsonSMS);
                    channel.basicAck(message.getEnvelope().getDeliveryTag(), false);
                },
                consumerTag -> {
                });
    }
}
