package io.spring.cloud.dbfile.resttemplate;

import io.spring.cloud.dbfile.dto.Dept;
import io.spring.cloud.dbfile.dto.Dept2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;

@Slf4j
@Service
public class RestTemplateservice {

    private final RestTemplate restTemplate;

    public RestTemplateservice(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void getForObject(){
        URI uri = UriComponentsBuilder
                .fromUriString("http://localhost:9090")
                .path("/server")
                .queryParam("d_name","김지인")
                .queryParam("loc","서울")
                .encode()
                .build()
                .toUri();
        log.info("uri : {}",uri);

       // RestTemplate restTemplate = new RestTemplate();
        Dept dept = restTemplate.getForObject(uri, Dept.class);
        log.info("dept : {}", dept);
    }


    public void customGetForObject(){
        URI uri = UriComponentsBuilder
                .fromUriString("http://localhost:9090")
                .path("/server")
                .queryParam("d_name","김지인")
                .queryParam("loc","서울")
                .encode()
                .build()
                .toUri();
        log.info("uri : {}",uri);

        // RestTemplate restTemplate = new RestTemplate();
        Dept dept = restTemplate.getForObject(uri, Dept.class);
        log.info("dept : {}", dept);
    }

    public void getForEntity(){
        URI uri = UriComponentsBuilder
                .fromUriString("http://localhost:9090")
                .path("/server/{path}")
                .queryParam("d_name","김지인")
                .queryParam("loc","서울")
                .encode()
                .build()
                .expand("dept")
                .toUri();
        log.info("uri : {}", uri);

     //   RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Dept> response = restTemplate.getForEntity(uri, Dept.class);
        log.info("{}",response.getStatusCode());
        log.info("{}",response.getHeaders());
        log.info("{}",response.getBody());
    }

    public void postForObject(){
        URI uri = UriComponentsBuilder
                .fromUriString("http://localhost:9090")
                .path("/server/{d_name}/{loc}")
                .encode()
                .build()
                .expand("d_name","loc")
                .toUri();
        log.info("uri : {}", uri);

     //   RestTemplate restTemplate = new RestTemplate();
        Dept dept = new Dept();
        dept.setD_name("김지인");
        dept.setLoc("서울");
        Dept response = restTemplate.postForObject(uri, dept, Dept.class);
        log.info("response : {}", response);
    }

    public void customPostForObject(){
        URI uri = UriComponentsBuilder
                .fromUriString("http://localhost:9090")
                .path("/server/{d_name}/{loc}")
                .encode()
                .build()
                .expand("d_name","loc")
                .toUri();
        log.info("uri : {}", uri);

        ArrayList<String> references = new ArrayList<>();
        ArrayList<String> links = new ArrayList<>();

        restTemplate.getMessageConverters().add(new StringHttpMessageConverter());
        for (int i = 0; i < 10; i++) {
            ResponseEntity<Dept2> resource = restTemplate.getForEntity(references.get(i), Dept2.class);
            links.add(resource.getBody().toString());
        }
        log.info("links: {}", links);

        // RestTemplate restTemplate = new RestTemplate();
       // Dept dept = new Dept();
       // dept.setD_name("김지인");
       // dept.setLoc("서울");
       // Dept response = restTemplate.postForObject(uri, dept, Dept.class);

    }

    public void postForEntity(){
        URI uri = UriComponentsBuilder
                .fromUriString("http://localhost:9090")
                .path("/api/{path}")
                .encode()
                .build()
                .expand("dept")
                .toUri();
        log.info("uri : {}", uri);

        Dept dept = new Dept();
        dept.setD_name("홍길동");
        dept.setLoc("서울");

     //   RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Dept> response = restTemplate.postForEntity(uri, dept, Dept.class);
        log.info("{}",response.getStatusCode());
        log.info("{}",response.getHeaders());
        log.info("{}",response.getBody());
    }

    public void exchange(){
        URI uri = UriComponentsBuilder
                .fromUriString("http://localhost:9090")
                .path("/server/header")
                .encode()
                .build()
               // .expand("dept")
                .toUri();
        log.info("uri : {}", uri);

        Dept dept = new Dept();
        dept.setD_name("홍길동");
        dept.setLoc("대전");

        RequestEntity<Dept> req = RequestEntity
                .post(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .header("x-authorization","my-header")
                .body(dept);

        ResponseEntity<Dept> response = restTemplate.exchange(req, new ParameterizedTypeReference<>(){});
        log.info("{}",response.getStatusCode());
        log.info("{}",response.getHeaders());
        log.info("{}",response.getBody());
    }

    public void exchange2(){
        RestTemplate restTemplate = new RestTemplate();

        URI uri = UriComponentsBuilder
                .fromUriString("http://localhost:9090")
                .path("/server/header")
                .encode()
                .build()
                .toUri();
        log.info("uri : {}", uri);

        //  Dept dept = new Dept();
        //  dept.setD_name("홍길동");
        //  dept.setLoc("대전");
        HttpHeaders headers = new HttpHeaders();  //debug >> header = 0
        Dept requestBody = new Dept("d_name", "loc");
        HttpEntity<Dept> entity = new HttpEntity<>(requestBody,headers); // Request Body로 등록


        ResponseEntity<Dept> response = restTemplate.exchange("http://localhost:9090/server/header",
                HttpMethod.POST,
                entity,
                Dept.class);


        RequestEntity<Dept> req = RequestEntity
                .post(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .header("x-authorization","headers")
                .body(requestBody);


        //ResponseEntity<Dept> response2 = restTemplate.exchange(req, new ParameterizedTypeReference<>(){});
        log.info("{}",response.getStatusCode());
        log.info("{}",response.getHeaders());
        log.info("{}",response.getBody());
    }





}
