package com.arturjarosz.fixmybudget;

import com.arturjarosz.fixmybudget.properties.AccountStatementFileProperties;
import com.arturjarosz.fixmybudget.properties.CategoryProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

@EnableConfigurationProperties({AccountStatementFileProperties.class, CategoryProperties.class})
@RequiredArgsConstructor
@SpringBootApplication
@ComponentScan("com.arturjarosz")
public class FixMyBudgetApplication {

    public static void main(String[] args) {
        SpringApplication.run(FixMyBudgetApplication.class, args);
    }


}
