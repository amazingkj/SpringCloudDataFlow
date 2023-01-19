package io.spring.cloud.partition;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.task.configuration.EnableTask;

@EnableTask //Task 실행 정보 저장 TaskRepository 설정
@EnableBatchProcessing //배치 job 기본 설정 제공
@SpringBootApplication
public class PartitionApplication {

	public static void main(String[] args) {
		SpringApplication.run(PartitionApplication.class, args);
	}

}
