package com.sju18.petmanagement.domain.account.api;

import com.sju18.petmanagement.domain.account.dao.Account;
import com.sju18.petmanagement.domain.account.application.JwtUserDetailsService;
import com.sju18.petmanagement.domain.account.dto.LoginRequestDto;
import com.sju18.petmanagement.domain.account.dto.LoginResponseDto;
import com.sju18.petmanagement.global.config.security.JwtTokenUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
public class LoginController {

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private JwtUserDetailsService userDetailService;

    @PostMapping("/api/account/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody LoginRequestDto loginRequestDto) {
        final Account account = userDetailService.authenticateByUsernameAndPassword
                (loginRequestDto.getUsername(), loginRequestDto.getPassword());
        final String token = jwtTokenUtil.generateToken(account.getUsername());
        return ResponseEntity.ok(new LoginResponseDto(token));
    }

}
