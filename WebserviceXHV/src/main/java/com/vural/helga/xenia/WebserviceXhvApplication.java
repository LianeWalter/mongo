package com.vural.helga.xenia;

import org.apache.log4j.BasicConfigurator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class WebserviceXhvApplication extends SpringBootServletInitializer {
	
	@Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(WebserviceXhvApplication.class);
    }

	public static void main(String[] args) throws Exception {
		//Log4j Configurater
		BasicConfigurator.configure();
		SpringApplication.run(WebserviceXhvApplication.class, args);

	}
}
