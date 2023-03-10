package io.spring.cloud.filefile.job;

import com.thoughtworks.xstream.XStream;
import io.spring.cloud.filefile.dto.Dept;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.json.JacksonJsonObjectMarshaller;
import org.springframework.batch.item.json.JsonFileItemWriter;
import org.springframework.batch.item.json.builder.JsonFileItemWriterBuilder;
import org.springframework.batch.item.xml.StaxEventItemReader;
import org.springframework.batch.item.xml.builder.StaxEventItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.oxm.xstream.XStreamMarshaller;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;


@Slf4j
@RequiredArgsConstructor
@Configuration
public class FileFileJobConfig {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private static final int chunkSize = 100;
    private static final String FILE_PATH = "C:/amazing/DataflowProject/Maven/resources/";

    @Bean
    public Job FileFileJob() throws Exception {
        return jobBuilderFactory.get("FileFileJob")
                .incrementer(new DailyJobTimestamper())
                .start(FileFileJob_buildStep())
                .listener(new FiletoFileJobExecutionListener())
                .validator(new ParameterValidator())
                .build();
    }
    @Bean
    public Step FileFileJob_buildStep() throws Exception {
        return stepBuilderFactory.get("FileFileJob_buildStep")
                .<Dept, Dept>chunk(chunkSize)
                .reader(customXmlItemReader(null))
                .writer(JsonItemWriter(null))
                .build();
    }

    /*xml*/
    @Bean
    @StepScope
    public StaxEventItemReader<Dept> customXmlItemReader(@Value("#{jobParameters[inputfile]}") String inputfile) {

        return new StaxEventItemReaderBuilder<Dept>()
                .name("customXmlItemReader")
                .encoding("UTF-8")
                .resource(new FileSystemResource(FILE_PATH+inputfile))
                .addFragmentRootElements("Dept")
                .unmarshaller(itemMarshaller())
                .build();
    }

    public XStreamMarshaller itemMarshaller() {
        Map<String, Class<?>> aliases = new HashMap<>();

        aliases.put("Dept", Dept.class); // tag ?????? ??????
        aliases.put("dept_no", Integer.class); // ?????????????????? ??? ????????? ??????
        aliases.put("d_name", String.class);
        aliases.put("loc", String.class);
        aliases.put("etc", String.class);

        XStreamMarshaller xStreamMarshaller = new XStreamMarshaller();
        xStreamMarshaller.setAliases(aliases);

        //?????? ??????????????? ?????? ?????? ??????
        XStream xStream = xStreamMarshaller.getXStream();
        xStream.allowTypes(new Class[] {
                Dept.class
        });

        return xStreamMarshaller;
    }


    @Bean
    @StepScope
    public JsonFileItemWriter<Dept> JsonItemWriter(@Value("#{jobParameters[outputfile]}") String outputfile) throws IOException {

        return new JsonFileItemWriterBuilder<Dept>()
                .name("JsonItemWriter")
                .jsonObjectMarshaller(new JacksonJsonObjectMarshaller<>())
                .resource(new FileSystemResource(FILE_PATH+outputfile))
                .build();


    }

}
