package cn.geekyspace.rabbitmq;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SpringbootApplicationTests {

    @Autowired
    private MessageProducer messageProducer;

    @Test
    void testSendMsg() {
        messageProducer.sendMessages(new Employee("1001", "张三", 25));
    }

}
