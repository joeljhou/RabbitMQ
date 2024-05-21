package cn.geekyspace.rabbitmq.exchange;

import cn.geekyspace.rabbitmq.News;
import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Date;

/**
 * 新闻生产者，生产者针对交换机发送消息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewsProducer {

    private RabbitTemplate rabbitTemplate;
    private static final Gson gson = new Gson();

    // 发布新闻
    public void sendNews(String routingKey, News news) {
        rabbitTemplate.convertAndSend(routingKey, gson.toJson(news));
        System.out.println("新闻发送成功，标题: " + news.getTitle());
    }

    public static void main(String[] args) {
        // 初始化IOC容器
        ApplicationContext ctx = new ClassPathXmlApplicationContext("classpath:applicationContext.xml");
        NewsProducer np = (NewsProducer) ctx.getBean("newsProducer");

        // 发布新闻
        np.sendNews("us.20240513", new News("新华社", "GPT-4o简介", "GPT-4o立即试用", new Date()));
        np.sendNews("china.20240516", new News("36氪", "Kimi.ai", "帮你看更大的世界", new Date()));
    }

}
