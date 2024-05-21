package cn.geekyspace.rabbitmq;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
public class MessageConsumer {

    private static final Gson gson = new Gson();

    /**
     * 消费者监听消息，并处理接收到的消息
     */
    // @RabbitHandler注解，标识该方法是 RabbitMQ 的消息处理方法
    @RabbitHandler
    // @RabbitListener注解，标识该方法是 RabbitMQ 的消息监听器
    @RabbitListener(bindings = {
            // 绑定到指定的队列，从指定的交换机接收消息，使用指定的路由键进行绑定。
            @QueueBinding(
                    value = @Queue(value = "springboot-queue", declare = "true"),
                    exchange = @Exchange(value = "springboot-exchange", declare = "true", type = "topic"),
                    key = "#")
    })
    // 可以使用@Payload注解，标识该方法的参数是消息体
    public void receiveMessages(@Payload String message, Channel channel,
                                @Headers Map<String, Object> headers) {
        System.out.println("===========================");
        Employee employee = gson.fromJson(message, Employee.class);
        System.out.println("接收到消息：员工编号：" + employee.getNumber()
                + "，员工姓名：" + employee.getName()
                + "，员工年龄：" + employee.getAge());
        try {
            // 手动ack确认
            channel.basicAck((Long) headers.get(AmqpHeaders.DELIVERY_TAG), false);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("===========================");
    }

}