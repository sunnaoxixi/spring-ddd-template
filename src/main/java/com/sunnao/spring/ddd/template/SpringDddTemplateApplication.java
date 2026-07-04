package com.sunnao.spring.ddd.template;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.sunnao.spring.ddd.template.infrastructure.**.mysql.mapper")
@SpringBootApplication
public class SpringDddTemplateApplication {

    static void main(String[] args) {
        SpringApplication.run(SpringDddTemplateApplication.class, args);
    }

}
