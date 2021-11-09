package com.sju18.petmanagement.domain.main.api;

/*
* HomeController 명세
* 주요기능: 서버 URL의 최상위 경로("/")로 요청이 왔을 때 해당 요청을 catch 함
* */

// 의존성 패키지 import
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {
    @RequestMapping("/")
    public String hello() {
        return "hello";
    }
}
