package io.spring.cloud.statistics;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.task.configuration.EnableTask;

@EnableTask
@EnableBatchProcessing
@SpringBootApplication
public class MakeStatisticsApplication {

	public static void main(String[] args) {
		SpringApplication.run(MakeStatisticsApplication.class, args);
	}

}
