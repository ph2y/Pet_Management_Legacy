package com.sju18.PetManagement.domain.account.api;

import com.sju18.PetManagement.domain.account.dao.Account;
import com.sju18.PetManagement.domain.account.application.JwtUserDetailsService;
import com.sju18.PetManagement.domain.account.dto.LoginRequestDTO;
import com.sju18.PetManagement.domain.account.dto.LoginResponseDTO;
import com.sju18.PetManagement.global.config.security.JwtTokenUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
public class JwtAuthenticationController {

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private JwtUserDetailsService userDetailService;

    @PostMapping("/api/account/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody LoginRequestDTO loginRequestDTO) throws Exception {
        final Account account = userDetailService.authenticateByUsernameAndPassword
                (loginRequestDTO.getUsername(), loginRequestDTO.getPassword());
        final String token = jwtTokenUtil.generateToken(account.getUsername());
        return ResponseEntity.ok(new LoginResponseDTO(token));
    }

}
