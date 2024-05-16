package cn.geekyspace.rabbitmq.workqueue;

import cn.geekyspace.rabbitmq.utils.RabbitConstant;
import cn.geekyspace.rabbitmq.utils.RabbitUtils;
import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.concurrent.TimeoutException;

/**
 * 工作队列模式适用于需要处理大量消息的场景，例如：订单系统中需要发送大量短信通知。
 */
public class OrderSystem {

    private static final Gson gson = new Gson();

    public static void main(String[] args) throws IOException, TimeoutException {
        try (Connection connection = RabbitUtils.getConnection();
             Channel channel = connection.createChannel()) {

            channel.queueDeclare(RabbitConstant.QUEUE_SMS, true, false, false, null);

            for (int i = 1; i <= 100; i++) {
                SMS sms = new SMS("12306", randomPhoneNumber(), "您的车票已预订成功。订单号：" + i);
                String jsonMessage = gson.toJson(sms);
                channel.basicPublish("", RabbitConstant.QUEUE_SMS, null, jsonMessage.getBytes(StandardCharsets.UTF_8));
                System.out.println(" [x] 发送 '" + jsonMessage + "'");
            }
            System.out.println("发送数据成功");
        }
    }

    private static String randomPhoneNumber() {
        Random random = new Random();
        StringBuilder phoneNumber = new StringBuilder();

        // 生成11位手机号码，以1开头
        phoneNumber.append("1");
        for (int i = 0; i < 10; i++) {
            phoneNumber.append(random.nextInt(10));
        }

        return phoneNumber.toString();
    }
}
