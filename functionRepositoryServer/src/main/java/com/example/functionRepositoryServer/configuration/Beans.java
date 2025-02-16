package com.example.functionRepositoryServer.configuration;


import com.example.functionRepositoryServer.service.MathModels.*;
import com.example.functionRepositoryServer.service.Tools.AnaliseExpression;
import com.example.functionRepositoryServer.service.Tools.PrepareExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class Beans {
    private List<Operation> operations;

    public Beans() {
        operations = new ArrayList<>();
        operations.add(new ModelFactorial());
        operations.add(new ModelSquare());
        operations.add(new ModelDivitev2());
        operations.add(new ModelMultiplyv2());
        operations.add(new ModelMinusv2());
        operations.add(new ModelPlusv2());
    }

    @Bean
    public AnaliseExpression analiseExpression(){
        return new AnaliseExpression(operations);
    }

    @Bean
    public PrepareExpression prepareExpression(){
        return new PrepareExpression();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public HttpHeaders httpHeaders() {
        return new HttpHeaders();
    }
}
