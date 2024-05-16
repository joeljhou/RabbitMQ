package cn.geekyspace.rabbitmq.confirm;

import cn.geekyspace.rabbitmq.utils.RabbitConstant;
import cn.geekyspace.rabbitmq.utils.RabbitUtils;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class WechatPaymentConsumer {
    public static void main(String[] args) throws IOException, TimeoutException {
        Connection connection = RabbitUtils.getConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(RabbitConstant.QUEUE_WECHAT, true, false, false, null);
        channel.queueBind(RabbitConstant.QUEUE_WECHAT, RabbitConstant.EXCHANGE_PAYMENT, "wechat.*");

        channel.basicConsume(RabbitConstant.QUEUE_WECHAT, false, (consumerTag, message) -> {
            System.out.println("微信收到订单：" + new String(message.getBody()));
            // 手动ack确认
            channel.basicAck(message.getEnvelope().getDeliveryTag(), false);
        }, consumerTag -> {
        });
    }
}
