package com.myapp.catching;

import com.fasterxml.jackson.core.type.TypeReference;
import com.myapp.catching.model.EngineObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.Resource;

import java.util.List;


@SpringBootApplication
public class StartApplication implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(StartApplication.class);

    @Autowired
    private EngineObjectRepository repository;

    @Value("classpath:json/test.json")
    private Resource resourceFile;

    public static void main(String[] args) {
        SpringApplication.run(StartApplication.class, args);
    }

    @Override
    public void run(String... args) {

        log.info("StartApplication...");
        ObjectMapper mapper = new ObjectMapper();
        try {
            List<EngineObject> objJsonList = mapper.readValue(resourceFile.getFile(), new TypeReference<List<EngineObject>>(){});
            objJsonList.forEach((n) -> repository.save(n));

        } catch (Exception e) {
            e.printStackTrace();
        }

//        repository.save(new Book("Node"));
//        repository.save(new Book("Python"));
//
        System.out.println("\nfindAll()");
        repository.findAll().forEach(x -> System.out.println(x));
//
//        System.out.println("\nfindById(1L)");
//        repository.findById(1l).ifPresent(x -> System.out.println(x));
//
//        System.out.println("\nfindByName('Node')");
//        repository.findByName("Node").forEach(x -> System.out.println(x));

    }

    @Bean
    public ObjectMapper mapper() {
        return new ObjectMapper();
    }
}