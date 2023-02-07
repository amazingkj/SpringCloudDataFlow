package io.spring.cloud.quartz;

import io.spring.cloud.quartz.parameters.CustomJobParameter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StringUtils;

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

		if(!StringUtils.hasText(jobParameter.getName())) {
			System.out.println("++++++ getName {}"+jobParameter.getName());
		}

		if(!StringUtils.hasText(jobParameter.getArgument())) {
			System.out.println("++++++ getArgument {}"+jobParameter.getArgument());
		}
		//널인데 통과하고있음
		if(!StringUtils.hasText(jobParameter.getProperties())){
			System.out.println("++++++ getProperties {}"+jobParameter.getProperties());
		}

		if(!StringUtils.isEmpty(jobParameter.getName())) {
			System.out.println("++++++ getName {}"+jobParameter.getName());
		}

		if(!StringUtils.isEmpty(jobParameter.getArgument())) {
			System.out.println("++++++ getArgument {}"+jobParameter.getArgument());
		}
		//널인데 통과하고있음
		if(!StringUtils.isEmpty(jobParameter.getProperties())){
			System.out.println("++++++ getProperties {}"+jobParameter.getProperties());
		}


	}




}
