package com.github.dadiyang.autologging.test;

import com.github.dadiyang.autologging.aop.annotation.AutoLogAll;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author dadiyang
 * @since 2020/3/1
 */
@AutoLogAll
@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

}
