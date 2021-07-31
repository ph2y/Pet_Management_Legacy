package com.sju18.petmanagement.global.security;

import com.sju18.petmanagement.global.common.DtoMetadata;
import org.json.JSONObject;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    // 인증 실패시 응답
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException e) throws IOException {
        // error response 로우레벨 생성
        PrintWriter resWriter = response.getWriter();
        response.setStatus(401);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        // response body 설정 및 전송
        JSONObject dtoMetadata = new JSONObject(new DtoMetadata(e.getMessage(), e.getClass().getName()));
        JSONObject resDto = new JSONObject();
        resDto.put("_metadata", dtoMetadata);
        resWriter.write(resDto.toString());
        resWriter.flush();
        resWriter.close();
    }

}