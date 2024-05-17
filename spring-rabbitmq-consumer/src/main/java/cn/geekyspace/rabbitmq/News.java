package cn.geekyspace.rabbitmq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class News implements Serializable {

    private String source;
    private String title;
    private String content;
    private Date createTime;

}
