package io.spring.cloud.ftp;

import io.spring.cloud.ftp.job.CustomJobParameter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class HttpApplicationTests {
    @Autowired
    CustomJobParameter jobParameter;
	@Test
	void contextLoads() {

    System.out.println(jobParameter.toString());
    }
}
