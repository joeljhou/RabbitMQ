package cn.geekyspace.rabbitmq;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MessageProducer {

    // 构造函数注入
    private final RabbitTemplate rabbitTemplate;

    private static final Gson gson = new Gson();

    RabbitTemplate.ConfirmCallback confirmCallback = (correlationData, ack, cause) -> {
        System.out.println("消息id:" + correlationData);
        System.out.println("ack:" + ack);
        if (ack) {
            System.out.println("消息发送确认成功");
        } else {
            System.out.println("消息发送确认失败:" + cause);
        }
    };

    RabbitTemplate.ReturnsCallback returnCallback = returnedMessage -> {
        System.out.println("========发送失败回掉========");
        System.out.println("退回编码: " + returnedMessage.getReplyCode() + ", 退回描述: " + returnedMessage.getReplyText());
        System.out.println("交换机: " + returnedMessage.getExchange() + ", 路由键：" + returnedMessage.getRoutingKey());
        System.out.println("消息主体: " + new String(returnedMessage.getMessage().getBody()));
        System.out.println("===========================");
    };

    // 生产者发送消息
    public void sendMessages(Employee employee) {

        // 消息发送确认，确认消息是否到达broker服务器
        rabbitTemplate.setConfirmCallback(confirmCallback);

        // 消息发送失败返回到队列中
        // 必须配置 spring.rabbitmq.template.mandatory=true 才能使用
        rabbitTemplate.setReturnsCallback(returnCallback);

        // 消息的附加信息，即自定义id
        final CorrelationData cd = new CorrelationData(employee.getNumber() + "-" + System.currentTimeMillis());
        rabbitTemplate.convertAndSend("springboot-exchange", "hr.employee", gson.toJson(employee), cd);
    }
}
