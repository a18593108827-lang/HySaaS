package com.hysaas;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** 应用启动入口 */
@SpringBootApplication(scanBasePackages = "com.hysaas")
@MapperScan("com.hysaas.**.mapper")
public class HySaasApplication {

    public static void main(String[] args) {
        SpringApplication.run(HySaasApplication.class, args);
    }
}
