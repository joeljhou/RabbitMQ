package cn.geekyspace.rabbitmq.confirm;

import cn.geekyspace.rabbitmq.utils.RabbitConstant;
import cn.geekyspace.rabbitmq.utils.RabbitUtils;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * 消息确认机制适用于需要保证消息可靠性传递的场景，例如：金融系统中支付订单。
 */
public class PaymentSystem {
    public static void main(String[] args) throws IOException, TimeoutException {

        //注意⚠️：关闭连接就无法监听回掉
        Connection connection = RabbitUtils.getConnection();
        Channel channel = connection.createChannel();

        // 开启confirm监听模式
        channel.confirmSelect();

        // 添加消息确认监听器
        channel.addConfirmListener(
                // ackCallback
                (deliveryTag, multiple) -> {
                    System.out.println("订单已被Broker接收，投递标签：" + deliveryTag);
                },
                // nackCallback
                (deliveryTag, multiple) -> {
                    System.out.println("订单已被Broker拒收，投递标签：" + deliveryTag);
                });

        // 添加消息退回监听器
        channel.addReturnListener(returnMessage -> {
            System.out.println("========支付订单被退回========");
            System.out.println("退回编码：" + returnMessage.getReplyCode() + "，退回描述：" + returnMessage.getReplyText());
            System.out.println("交换机：" + returnMessage.getExchange() + "，路由键：" + returnMessage.getRoutingKey());
            System.out.println("退回主题：" + new String(returnMessage.getBody()));
            System.out.println("===========================");
        });

        // 发送支付订单消息
        LinkedHashMap<String, String> paymentOrder = new LinkedHashMap<>();
        paymentOrder.put("alipay.20991011", "支付宝订单20991011");
        paymentOrder.put("wechat.20991011", "微信订单20991011");
        paymentOrder.put("unionpay.20991011", "银联订单20991011");

        for (Map.Entry<String, String> entry : paymentOrder.entrySet()) {
            String routingKey = entry.getKey();
            String message = entry.getValue();
            // 注意⚠️：第三个参数是 mandatory，用于消息的退回
            // 当为 true  时，如果消息无法正常投递则 return 回生产者；
            // 当为 false 时，直接将消息放弃；
            channel.basicPublish(RabbitConstant.EXCHANGE_PAYMENT, routingKey, true, null, message.getBytes());
            System.out.println(" [x] 发送 '" + routingKey + "':'" + message + "'");
        }

    }
}
