package cn.geekyspace.rabbitmq.consumer;

import cn.geekyspace.rabbitmq.News;
import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 新闻消费者，消费者从队列中接收消息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewsConsumer implements MessageListener {

    private RabbitTemplate rabbitTemplate;
    private static final Gson gson = new Gson();

    @Override
    public void onMessage(Message message) {
        // 处理接收到的消息
        final News news = gson.fromJson(new String(message.getBody()), News.class);
        System.out.printf("接收到最新新闻: 标题-%s 内容-%s%n", news.getTitle(), news.getContent());
    }

    public static void main(String[] args) {
        //初始化IOC容器
        ApplicationContext ctx = new ClassPathXmlApplicationContext("classpath:applicationContext.xml");
        RabbitTemplate rabbitTemplate = ctx.getBean(RabbitTemplate.class);

        // 创建消费者
        NewsConsumer newsConsumer = new NewsConsumer();

        // 设置消息监听容器
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(rabbitTemplate.getConnectionFactory());
        container.setQueueNames("topicQueue"); // 设置要监听的队列名
        container.setMessageListener(newsConsumer);

        // 启动监听
        container.start();
    }
}
