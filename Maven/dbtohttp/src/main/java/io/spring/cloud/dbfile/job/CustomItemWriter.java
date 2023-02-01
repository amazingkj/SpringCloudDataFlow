//package io.spring.cloud.dbfile.job;
//
//import io.spring.cloud.dbfile.dto.Dept;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.batch.item.ItemWriter;
//import org.springframework.batch.support.transaction.TransactionAwareProxyFactory;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.client.RestTemplate;
//
//import java.util.List;
//@Slf4j
//public class CustomItemWriter<T> implements ItemWriter<T> {
//    private final RestTemplate restTemplate = null;
//
//
//    List<T> output = TransactionAwareProxyFactory.createTransactionalList();
//
//    public CustomItemWriter() {
//        this.restTemplate = restTemplate;
//    }
//
//    public void write(List<? extends T> items) throws Exception {
//       output.addAll(items);
//       ResponseEntity<Dept> dept = restTemplate.postForEntity("http://localhost:9090/server/header",output, Dept.class);
//       log.info("Status code is: {}", dept.getStatusCode());
//       log.info("output is: {}", dept.getStatusCode());
//
//   }
//
//    public List<T> getOutput() {
//        return output;
//    }
//}
