package org.mrshoffen.tasktracker.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

@SpringBootApplication
public class TaskTrackerApiGatewayApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(TaskTrackerApiGatewayApplication.class, args);
        Environment bean = context.getBean(Environment.class);

    }

}
