package com.example.ems;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.web.bind.annotation.CrossOrigin;
@SpringBootApplication
//@EnableDiscoveryClient
public class EmsApplication {
	public static void main(String[] args) {
		SpringApplication.run(EmsApplication.class, args);
	}
}