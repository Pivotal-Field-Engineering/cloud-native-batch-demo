package io.pivotal.dragonstonefinance.ratingsloader;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.task.configuration.EnableTask;

@EnableTask
@SpringBootApplication
public class RatingsLoaderApplication {

	public static void main(String[] args) {
		SpringApplication.run(RatingsLoaderApplication.class, args);
	}

}
