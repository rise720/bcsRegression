package com.cathay.bcsRegression;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.cathay.bcsRegression.dao")
public class BcsRegressionApplication {

	public static void main(String[] args) {
		SpringApplication.run(BcsRegressionApplication.class, args);
	}

}
