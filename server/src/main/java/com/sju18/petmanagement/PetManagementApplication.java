package com.sju18.petmanagement;

/*
* PetManagementApplication 명세
* 주요기능: 어플리케이션의 최상단 entrypoint
* */

// 의존성 패키지 import
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication(scanBasePackages = "com.sju18")
public class PetManagementApplication extends SpringBootServletInitializer {
	public static void main(String[] args) {
		SpringApplication.run(PetManagementApplication.class, args);
	}

}
