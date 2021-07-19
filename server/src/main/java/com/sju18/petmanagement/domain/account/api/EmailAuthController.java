package com.sju18.petmanagement.domain.account.api;

import com.sju18.petmanagement.domain.account.dao.AccountRepository;
import com.sju18.petmanagement.domain.account.dto.SendAuthCodeRequestDto;
import com.sju18.petmanagement.domain.account.dto.SendAuthCodeResponseDto;
import com.sju18.petmanagement.domain.account.dto.VerifyAuthCodeRequestDto;
import com.sju18.petmanagement.domain.account.dto.VerifyAuthCodeResponseDto;
import com.sju18.petmanagement.global.util.email.EmailService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class EmailAuthController {
    private final EmailService emailService;

    final AccountRepository accountRepository;

    @PostMapping("/api/account/sendauthcode")
    public ResponseEntity<?> sendAuthCodeToEmail(@RequestBody SendAuthCodeRequestDto sendAuthCodeRequestDto) {
        if (accountRepository.existsByEmail(sendAuthCodeRequestDto.getEmail())) {
            return ResponseEntity.badRequest().body(new SendAuthCodeResponseDto("Email already exists"));
        }

        try {
            emailService.sendVerificationMessage(sendAuthCodeRequestDto.getEmail());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(new SendAuthCodeResponseDto("Verification code send failed"));
        }

        return ResponseEntity.ok(new SendAuthCodeResponseDto("Email verification code sent"));
    }

    @PostMapping("/api/account/verifyauthcode")
    public ResponseEntity<?> verifyAuthCode(@RequestBody VerifyAuthCodeRequestDto verifyAuthCodeRequestDto) {
        if(EmailService.authCode.equals(verifyAuthCodeRequestDto.getCode())) {
            return ResponseEntity.ok(new VerifyAuthCodeResponseDto("Email verification success"));
        }
        else{
            return ResponseEntity.badRequest().body(new VerifyAuthCodeResponseDto("Email verification failure"));
        }
    }
}
