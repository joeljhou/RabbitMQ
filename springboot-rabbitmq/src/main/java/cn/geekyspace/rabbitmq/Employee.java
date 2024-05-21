package cn.geekyspace.rabbitmq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Employee implements Serializable {

    // 员工编号
    private String number;

    // 员工姓名
    private String name;

    // 员工年龄
    private Integer age;

}
