package io.pivotal.dragonstonefinance.tradesloader;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.task.configuration.EnableTask;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@EnableTask
@EnableDiscoveryClient
@SpringBootApplication
@EnableCircuitBreaker
public class TradesLoaderApplication {

	public static void main(String[] args) {
		SpringApplication.run(TradesLoaderApplication.class, args);
	}

	@Bean
	@LoadBalanced
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

}
