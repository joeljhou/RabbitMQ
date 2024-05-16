package cn.geekyspace.rabbitmq.confirm;

import cn.geekyspace.rabbitmq.utils.RabbitConstant;
import cn.geekyspace.rabbitmq.utils.RabbitUtils;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class AlipayPaymentConsumer {
    public static void main(String[] args) throws IOException, TimeoutException {
        Connection connection = RabbitUtils.getConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(RabbitConstant.QUEUE_ALIPAY, true, false, false, null);
        channel.queueBind(RabbitConstant.QUEUE_ALIPAY, RabbitConstant.EXCHANGE_PAYMENT, "alipay.*");

        channel.basicConsume(RabbitConstant.QUEUE_ALIPAY, false,
                // 消费者接收消息回调
                (consumerTag, message) -> {
                    System.out.println("支付宝收到订单：" + new String(message.getBody()));
                    // 手动ack确认
                    channel.basicAck(message.getEnvelope().getDeliveryTag(), false);
                },
                // 消费者取消消费回调
                consumerTag -> {
                });
    }
}
