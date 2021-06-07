package com.sju18.petmanagement.domain.account.api;

import com.sju18.petmanagement.domain.account.dto.SignupRequestDTO;
import com.sju18.petmanagement.domain.account.dto.SignupResponseDTO;
import lombok.RequiredArgsConstructor;

import com.sju18.petmanagement.domain.account.dao.Account;
import com.sju18.petmanagement.domain.account.dao.AccountRepository;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SignupController {
    private static final Logger logger = LogManager.getLogger();

    final AccountRepository accountRepository;
    final PasswordEncoder encode;

    @PostMapping("/api/account/signup")
    public ResponseEntity<?> registerAccount(@RequestBody SignupRequestDTO signupRequestDTO) {
        // Account 객체 생성
        Account newAccount = Account.createAccount(
                signupRequestDTO.getUsername(),
                encode.encode(signupRequestDTO.getPassword()),
                signupRequestDTO.getEmail(),
                signupRequestDTO.getName(),
                signupRequestDTO.getPhone(),
                signupRequestDTO.getPhoto()
        );
        
        // 중복 확인
        if (accountRepository.existsByUsername(newAccount.getUsername())) {
            return ResponseEntity.badRequest().body(new SignupResponseDTO("Username already exists"));
        }

        // DB에 계정정보 저장
        try {
            accountRepository.save(newAccount);
            return ResponseEntity.ok(new SignupResponseDTO("Account register success"));
        } catch (Exception e) {
            logger.warn(e.getMessage());
            return ResponseEntity.status(500).body(new SignupResponseDTO(e.getMessage()));
        }
    }

}


