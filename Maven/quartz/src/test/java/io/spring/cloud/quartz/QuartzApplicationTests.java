package io.spring.cloud.quartz;

import io.spring.cloud.quartz.parameters.CustomJobParameter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class QuartzApplicationTests {

	@Test
	void contextLoads() {
	}

		@Autowired
		CustomJobParameter jobParameter;

	@Test
	void valueTest(){
		System.out.println(jobParameter.toString());
		System.out.println(new CustomJobParameter().toString());

	}




}
