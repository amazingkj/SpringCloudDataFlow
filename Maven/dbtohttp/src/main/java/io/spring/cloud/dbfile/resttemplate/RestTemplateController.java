package io.spring.cloud.dbfile.resttemplate;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dept")
@RequiredArgsConstructor
public class RestTemplateController {


    private final RestTemplateservice restTemplateservice;

    //    @GetMapping("")
     //  public void get(){
     //      restTemplateservice.postForObject();
    // }

    @GetMapping("")
    public void post(){
        restTemplateservice.postForObject();
    }

}
