package com.akxy;

import com.akxy.configuration.DynamicDataSourceRegister;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@EnableScheduling
@SpringBootApplication
@Import(DynamicDataSourceRegister.class)
public class DataAccessApplication {

	private static ExecutorService executorService = Executors.newCachedThreadPool();

	public static void main(String[] args) {
		SpringApplication.run(DataAccessApplication.class, args);
	}


	public static void execute(Runnable thread) {
		executorService.execute(thread);
	}
}
