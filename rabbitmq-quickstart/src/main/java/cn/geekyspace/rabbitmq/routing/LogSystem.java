package cn.geekyspace.rabbitmq.routing;

import cn.geekyspace.rabbitmq.utils.RabbitConstant;
import cn.geekyspace.rabbitmq.utils.RabbitUtils;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * 路由模式适用于需要精确匹配消息的场景，例如：日志系统中根据日志级别将消息发送到不同的队列。
 */
public class LogSystem {
    public static void main(String[] args) throws IOException, TimeoutException {
        try (Connection connection = RabbitUtils.getConnection();
             Channel channel = connection.createChannel()) {

            LinkedHashMap<String, String> logs = new LinkedHashMap<>();
            logs.put("error", "error message");
            logs.put("warning", "warning message");
            logs.put("info", "info message");
            logs.put("debug", "debug message");

            for (Map.Entry<String, String> entry : logs.entrySet()) {
                String routingKey = entry.getKey();
                String message = entry.getValue();
                // 注意⚠️：第二个参数是 routingKey，用于消息的筛选
                channel.basicPublish(RabbitConstant.EXCHANGE_LOGS, routingKey, null, message.getBytes());
                System.out.println(" [x] 发送 '" + routingKey + "':'" + message + "'");
            }
        }
    }
}
