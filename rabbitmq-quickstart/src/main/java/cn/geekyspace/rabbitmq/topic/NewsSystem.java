package cn.geekyspace.rabbitmq.topic;

import cn.geekyspace.rabbitmq.utils.RabbitConstant;
import cn.geekyspace.rabbitmq.utils.RabbitUtils;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

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
