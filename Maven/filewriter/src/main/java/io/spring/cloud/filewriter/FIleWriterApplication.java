package io.spring.cloud.filewriter;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.task.configuration.EnableTask;

@EnableTask
@EnableBatchProcessing
@SpringBootApplication
public class FIleWriterApplication {

	public static void main(String[] args) {
		SpringApplication.run(FIleWriterApplication.class, args);
	}

}
