package com.example.csd;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CsdApplication  {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(CsdApplication.class);
        //app.setDefaultProperties(Collections
         //       .singletonMap("server.port", "8083"));
        app.run(args);


        //CsdServer server = new CsdServer();
        //SpringApplication.run(CsdApplication.class, args);

    }

/*
    @Override
    public void run(ApplicationArguments args) throws Exception {
        //CsdServer server = CsdServer.getInstance(id);
        //CsdClient client = CsdClient.getInstance(id);
    }*/
}
