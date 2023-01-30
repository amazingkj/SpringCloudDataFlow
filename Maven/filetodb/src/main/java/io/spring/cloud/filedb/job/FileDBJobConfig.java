package io.spring.cloud.filedb.job;

import com.thoughtworks.xstream.XStream;
import io.spring.cloud.filedb.dto.Dept;
import io.spring.cloud.filedb.dto.DeptDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.separator.SimpleRecordSeparatorPolicy;
import org.springframework.batch.item.json.JacksonJsonObjectReader;
import org.springframework.batch.item.json.JsonItemReader;
import org.springframework.batch.item.json.builder.JsonItemReaderBuilder;
import org.springframework.batch.item.xml.StaxEventItemReader;
import org.springframework.batch.item.xml.builder.StaxEventItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.oxm.xstream.XStreamMarshaller;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;


@Slf4j
@RequiredArgsConstructor
@Configuration
public class FileDBJobConfig {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final DataSource dataSource;
    private static final int chunkSize = 5;



    @Bean
    public Job FileDBJob() throws Exception {
        return jobBuilderFactory.get("FileDBJob")
                .incrementer(new RunIdIncrementer())
                //.start(FileDBJob_buildStep())
                //.start(XmlFileDBJob_buildStep())
                .start(JsonFileDBJob_buildStep())
                .build();
    }

    @Bean
    public Step FileDBJob_buildStep() throws Exception {
        return stepBuilderFactory.get("FileDBJob_buildStep")
                .<DeptDTO, Dept>chunk(chunkSize)
                .reader(FileDBJob_FileReader())
                .writer(jdbcBatchItemWriter())
                .build();
    }

    @Bean
    public Step XmlFileDBJob_buildStep() throws Exception {
        return stepBuilderFactory.get("XmlFileDBJob_buildStep")
                .<DeptDTO,Dept>chunk(chunkSize)
                .reader(customXmlItemReader())
                .writer(jdbcBatchItemWriter())
                .build();
    }

    @Bean
    public Step JsonFileDBJob_buildStep() throws Exception {
        return stepBuilderFactory.get("JsonFileDBJob_buildStep")
                .<DeptDTO, Dept>chunk(chunkSize)
                .reader(customJsonItemReader())
                .writer(jdbcBatchItemWriter())
                .build();
    }


    /*xml*/

    public StaxEventItemReader<DeptDTO> customXmlItemReader() {
        return new StaxEventItemReaderBuilder<DeptDTO>()
                .name("customXmlItemReader")
                .resource(new ClassPathResource("csvInput/dept_xmlInput.xml"))
                .addFragmentRootElements("Dept")
                .unmarshaller(itemMarshaller())
                .build();
    }


    public ItemWriter<DeptDTO> customXmlItemWriter() {

        log.info("++++XmlItemWriter++++");
        return items -> {
            for (DeptDTO item : items) {
                log.info("{}",item.toString());
            }
        };
    }


    public XStreamMarshaller itemMarshaller() {
        Map<String, Class<?>> aliases = new HashMap<>();


        aliases.put("Dept", DeptDTO.class); // tag 전체 타입
        aliases.put("deptNo", Integer.class); // 두번째부터는 각 항목의 타입
        aliases.put("dName", String.class);
        aliases.put("loc", String.class);
        aliases.put("etc", String.class);

        XStreamMarshaller xStreamMarshaller = new XStreamMarshaller();
        xStreamMarshaller.setAliases(aliases);

        //보안 프레임워크 구성 에러 해결
        XStream xStream = xStreamMarshaller.getXStream();
        xStream.allowTypes(new Class[] {
                DeptDTO.class
        });

        return xStreamMarshaller;
    }

    /*json*/

    public JsonItemReader<DeptDTO> customJsonItemReader() {
        return new JsonItemReaderBuilder<DeptDTO>()
                .name("customJsonItemReader")
                .jsonObjectReader(new JacksonJsonObjectReader<>(DeptDTO.class))
                .resource(new ClassPathResource("csvInput/dept_JsonInput.json"))
                .build();
    }


    public ItemWriter<DeptDTO> customJsonItemWriter() {

        log.info("++++JsonItemWriter++++");
        return items -> {
            for (DeptDTO item : items) {
                log.info("{}",item.toString());
            }
        };
    }



    /*csv*/


    public FlatFileItemReader<DeptDTO> FileDBJob_FileReader(){
        return new FlatFileItemReaderBuilder<DeptDTO>()
                .name("FileDBJob_FileReader")
                .encoding("UTF-8")
                .resource(new ClassPathResource("csvInput/new_input.csv"))
                .delimited().delimiter(",")
                .names("deptNo","dName", "loc")
                .targetType(DeptDTO.class)
                .recordSeparatorPolicy(new SimpleRecordSeparatorPolicy() {
                    @Override
                    public String postProcess(String record) {
                        return record.trim();
                    }
                })
                .build();
    }



    private ItemWriter<Dept> jdbcBatchItemWriter() {
        JdbcBatchItemWriter<Dept> itemWriter = new JdbcBatchItemWriterBuilder<Dept>()
                .dataSource(dataSource)
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .sql("insert into dept(dept_no, d_name, loc, etc) values(:deptNo, :dName, :loc, :etc)")
                .build();

        itemWriter.afterPropertiesSet();

        return itemWriter;
    }




}
