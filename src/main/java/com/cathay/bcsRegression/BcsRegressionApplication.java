package com.cathay.bcsRegression;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.freemarker.FreeMarkerAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = {FreeMarkerAutoConfiguration.class})
@MapperScan("com.cathay.bcsRegression.dao")
@EnableScheduling
public class BcsRegressionApplication {

	public static void main(String[] args) {
		SpringApplication.run(BcsRegressionApplication.class, args);
	}

}
