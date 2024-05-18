package cn.geekyspace.exchange;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * <a href="https://github.com/rabbitmq/rabbitmq-server/discussions/11268">...</a>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext.xml")
public class RabbitAdminTest {

    @Autowired
    private RabbitAdmin rabbitAdmin;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    // 创建交换机
    @Test
    public void testCreateExchange() {
        rabbitAdmin.declareExchange(new DirectExchange("test.direct", true, false));
        rabbitAdmin.declareExchange(new FanoutExchange("test.fanout", true, false));
        rabbitAdmin.declareExchange(new TopicExchange("test.topic", true, false));
    }

    // 删除交换机
    @Test
    public void testDeleteExchange() {
        rabbitAdmin.deleteExchange("test.direct");
        rabbitAdmin.deleteExchange("test.fanout");
        rabbitAdmin.deleteExchange("test.topic");
    }

    // 创建队列
    @Test
    public void testCreateQueue() {
        rabbitAdmin.declareQueue(new Queue("test.direct.queue", true));
        rabbitAdmin.declareQueue(new Queue("test.fanout.queue", true));
        rabbitAdmin.declareQueue(new Queue("test.topic.queue", true));
    }

    // 删除队列
    @Test
    public void testDeleteQueue() {
        rabbitAdmin.deleteQueue("test.direct.queue");
        rabbitAdmin.deleteQueue("test.topic.queue");
        rabbitAdmin.deleteQueue("test.fanout.queue");
    }

    // 绑定队列
    @Test
    public void testBinding() {
        Binding directBinding = new Binding(
                "test.direct.queue", Binding.DestinationType.QUEUE,
                "test.direct", "test.direct.queue", null);
        Binding fanoutBinding = new Binding(
                "test.fanout.queue", Binding.DestinationType.QUEUE,
                "test.fanout", "#", null);
        Binding topicBinding = new Binding(
                "test.topic.queue", Binding.DestinationType.QUEUE,
                "test.topic", "#", null);
        rabbitAdmin.declareBinding(directBinding);
        rabbitAdmin.declareBinding(fanoutBinding);
        rabbitAdmin.declareBinding(topicBinding);
    }

    // 发送消息
    @Test
    public void testSendMessage() {
        // 直连交换机，用于简单模式和工作队列
        rabbitTemplate.convertAndSend("test.direct", "test.direct.queue", "Hello, RabbitMQ !");
        // 扇形交换机，用于发布订阅
        rabbitTemplate.convertAndSend("test.fanout", "", "长沙天气：晴");
        // 主题交换机，用于路由模式和主题模式
        rabbitTemplate.convertAndSend("test.topic", "china.news", "中国新闻");

        // q: Headers交换机工作原理是什么？
        // a: 通过消息头来路由消息，通过 x-match 参数来指定匹配规则，有 all 和 any 两种规则。
    }

    // 接收消息
    @Test
    public void testReceiveMessage() {
        Object directMessage = rabbitTemplate.receiveAndConvert("test.direct.queue");
        Object fanoutMessage = rabbitTemplate.receiveAndConvert("test.fanout.queue");
        Object topicMessage = rabbitTemplate.receiveAndConvert("test.topic.queue");
        System.out.println("directMessage = " + directMessage);
        System.out.println("fanoutMessage = " + fanoutMessage);
        System.out.println("topicMessage = " + topicMessage);
    }

}
