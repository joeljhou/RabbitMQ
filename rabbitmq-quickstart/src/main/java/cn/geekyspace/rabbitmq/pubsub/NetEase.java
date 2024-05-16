package cn.geekyspace.rabbitmq.pubsub;

import cn.geekyspace.rabbitmq.utils.RabbitConstant;
import cn.geekyspace.rabbitmq.utils.RabbitUtils;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import java.io.IOException;

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
