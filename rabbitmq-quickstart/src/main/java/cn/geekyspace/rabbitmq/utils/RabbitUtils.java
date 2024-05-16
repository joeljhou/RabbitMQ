package cn.geekyspace.rabbitmq.utils;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class RabbitUtils {

    private static final ConnectionFactory connectionFactory = new ConnectionFactory();

    static {
        connectionFactory.setHost("localhost");
        connectionFactory.setPort(5672);         //5672是RabbitMQ的默认端口号
        connectionFactory.setUsername("zhouyu");
        connectionFactory.setPassword("123456");
        connectionFactory.setVirtualHost("/geekyspace");
    }

    public static Connection getConnection() {
        Connection conn = null;
        try {
            conn = connectionFactory.newConnection();
            return conn;
        } catch (Exception e) {
            // 运行时异常
            throw new RuntimeException(e);
        }
    }
}
