package io.spring.cloud.ftp;

import org.apache.sshd.sftp.client.SftpClient;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.task.configuration.EnableTask;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.file.FileNameGenerator;
import org.springframework.integration.file.remote.session.CachingSessionFactory;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.integration.sftp.outbound.SftpMessageHandler;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;

import java.io.File;

@EnableTask
@EnableBatchProcessing
@SpringBootApplication
//@IntegrationComponentScan
public class SftpJavaApplication {

	public static void main(String[] args) {
		SpringApplication.run(SftpJavaApplication.class, args);
	}

//
//	public static void main(String[] args) {
//		ConfigurableApplicationContext context =
//				new SpringApplicationBuilder(SftpJavaApplication.class)
//						.web(false)
//						.run(args);
//		MyGateway gateway = context.getBean(MyGateway.class);
//		gateway.sendToSftp(new File("/foo/bar.txt"));
//	}
//
//	@Bean
//	public SessionFactory<SftpClient.DirEntry> sftpSessionFactory() {
//		DefaultSftpSessionFactory factory = new DefaultSftpSessionFactory(true);
//		factory.setHost("localhost");
//		factory.setPort(port);
//		factory.setUser("foo");
//		factory.setPassword("foo");
//		factory.setAllowUnknownKeys(true);
//		factory.setTestSession(true);
//		return new CachingSessionFactory<SftpClient.DirEntry>(factory);
//	}
//
//	@Bean
//	@ServiceActivator(inputChannel = "toSftpChannel")
//	public MessageHandler handler() {
//		SftpMessageHandler handler = new SftpMessageHandler(sftpSessionFactory());
//		handler.setRemoteDirectoryExpressionString("headers['remote-target-dir']");
//		handler.setFileNameGenerator(new FileNameGenerator() {
//
//			@Override
//			public String generateFileName(Message<?> message) {
//				return "handlerContent.test";
//			}
//
//		});
//		return handler;
//	}
//
//	@MessagingGateway
//	public interface MyGateway {
//
//		@Gateway(requestChannel = "toSftpChannel")
//		void sendToSftp(File file);
//
//	}

}
