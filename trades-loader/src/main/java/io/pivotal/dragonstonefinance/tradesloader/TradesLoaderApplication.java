package io.pivotal.dragonstonefinance.tradesloader;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.task.configuration.EnableTask;

@EnableTask
@SpringBootApplication
public class TradesLoaderApplication {

	public static void main(String[] args) {
		SpringApplication.run(TradesLoaderApplication.class, args);
	}

}
