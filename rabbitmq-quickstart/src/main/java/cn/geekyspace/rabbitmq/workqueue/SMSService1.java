package cn.geekyspace.rabbitmq.workqueue;

import cn.geekyspace.rabbitmq.utils.RabbitConstant;
import cn.geekyspace.rabbitmq.utils.RabbitUtils;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import java.io.IOException;

public class SMSService1 {
    public static void main(String[] args) throws IOException {
        Connection connection = RabbitUtils.getConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(RabbitConstant.QUEUE_SMS, true, false, false, null);
        // 注意⚠️：保证一次只分发一个，能者多劳
        channel.basicQos(1);
        channel.basicConsume(RabbitConstant.QUEUE_SMS, false,
                // 消费者接收消息回调
                (consumerTag, message) -> {
                    String jsonSMS = new String(message.getBody());
                    System.out.println("SMSService1-短信发送成功：" + jsonSMS);
                    // 手动ack确认
                    channel.basicAck(message.getEnvelope().getDeliveryTag(), false);
                },
                // 消费者取消消费回调
                consumerTag -> {
                });
    }
}
