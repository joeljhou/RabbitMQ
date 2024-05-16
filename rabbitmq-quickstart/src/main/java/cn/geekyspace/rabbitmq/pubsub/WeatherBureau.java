package cn.geekyspace.rabbitmq.pubsub;

import cn.geekyspace.rabbitmq.utils.RabbitConstant;
import cn.geekyspace.rabbitmq.utils.RabbitUtils;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

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
