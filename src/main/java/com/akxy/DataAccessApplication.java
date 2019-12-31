package com.akxy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import com.akxy.configuration.DynamicDataSourceRegister;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@Import(DynamicDataSourceRegister.class)
public class DataAccessApplication {

	public static void main(String[] args) {
		SpringApplication.run(DataAccessApplication.class, args);
	}

}
