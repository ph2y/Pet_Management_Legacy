package com.sju18.PetManagement;

/*
* PetManagementApplication 명세
* 주요기능: 어플리케이션의 최상단 entrypoint
* */

// 의존성 패키지 import
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PetManagementApplication {
	public static void main(String[] args) {
		SpringApplication.run(PetManagementApplication.class, args);
	}

}
