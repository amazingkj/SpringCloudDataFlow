package io.spring.cloud.partition.configuration;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.partition.PartitionHandler;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.deployer.resource.support.DelegatingResourceLoader;
import org.springframework.cloud.deployer.spi.task.TaskLauncher;
import org.springframework.cloud.task.batch.partition.DeployerPartitionHandler;
import org.springframework.cloud.task.batch.partition.DeployerStepExecutionHandler;
import org.springframework.cloud.task.batch.partition.PassThroughCommandLineArgsProvider;
import org.springframework.cloud.task.batch.partition.SimpleEnvironmentVariablesProvider;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;

import javax.sql.DataSource;
import java.util.*;

@Configuration
public class JobConfiguration {
    private static final int GRID_SIZE = 4;
    @Autowired
    public JobBuilderFactory jobBuilderFactory;
    @Autowired
    public StepBuilderFactory stepBuilderFactory;
    @Autowired
    public DataSource dataSource;
    @Autowired
    public JobRepository jobRepository;
    @Autowired
    private ConfigurableApplicationContext context;
    @Autowired
    private DelegatingResourceLoader resourceLoader;
    @Autowired
    private Environment environment;

    @Bean // (1)
    public PartitionHandler partitionHandler(TaskLauncher taskLauncher, JobExplorer jobExplorer) throws Exception {
        Resource resource = this.resourceLoader
                .getResource("maven://io.spring.cloud:partition:0.0.1-SNAPSHOT");

        DeployerPartitionHandler partitionHandler =
                new DeployerPartitionHandler(taskLauncher, jobExplorer, resource, "workerStep");

        List<String> commandLineArgs = new ArrayList<>(3);
        commandLineArgs.add("--spring.profiles.active=worker");
        commandLineArgs.add("--spring.cloud.task.initialize.enable=false");
        commandLineArgs.add("--spring.batch.initializer.enabled=false");
        partitionHandler
                .setCommandLineArgsProvider(new PassThroughCommandLineArgsProvider(commandLineArgs));
        partitionHandler
                .setEnvironmentVariablesProvider(new SimpleEnvironmentVariablesProvider(this.environment));
        partitionHandler.setMaxWorkers(1);
        partitionHandler.setApplicationName("PartitionedBatchJobTask");

        return partitionHandler;
    }

    @Bean // (2)
    @Profile("!worker")
    public Job partitionedJob(PartitionHandler partitionHandler) throws Exception {
        Random random = new Random();
        return this.jobBuilderFactory.get("partitionedJob" + random.nextInt())
                .start(step1(partitionHandler))
                .build();
    }

    @Bean // (3)
    public Step step1(PartitionHandler partitionHandler) throws Exception {
        return this.stepBuilderFactory.get("step1")
                .partitioner(workerStep().getName(), partitioner())
                .step(workerStep())
                .partitionHandler(partitionHandler)
                .build();
    }

    @Bean // (4)
    public Partitioner partitioner() {
        return new Partitioner() {
            @Override
            public Map<String, ExecutionContext> partition(int gridSize) {

                Map<String, ExecutionContext> partitions = new HashMap<>(gridSize);

                for (int i = 0; i < GRID_SIZE; i++) {
                    ExecutionContext context1 = new ExecutionContext();
                    context1.put("partitionNumber", i);

                    partitions.put("partition" + i, context1);
                }

                return partitions;
            }
        };
    }

    @Bean // (5)
    @Profile("worker")
    public DeployerStepExecutionHandler stepExecutionHandler(JobExplorer jobExplorer) {
        return new DeployerStepExecutionHandler(this.context, jobExplorer, this.jobRepository);
    }

    @Bean // (6)
    public Step workerStep() {
        return this.stepBuilderFactory.get("workerStep")
                .tasklet(workerTasklet(null))
                .build();
    }

    @Bean // (7)
    @StepScope
    public Tasklet workerTasklet(
            final @Value("#{stepExecutionContext['partitionNumber']}") Integer partitionNumber) {

        return new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                System.out.println("This tasklet ran partition: " + partitionNumber);

                return RepeatStatus.FINISHED;
            }
        };
    }
}
