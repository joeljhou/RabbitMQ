package cn.geekyspace.rabbitmq.workqueue;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SMS {
    private String name;
    private String mobile;
    private String content;

    // 自动生成 Getter、Setter、equals、hashCode 和 toString 方法

}
