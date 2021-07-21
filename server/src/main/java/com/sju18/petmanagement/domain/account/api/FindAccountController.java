package com.sju18.petmanagement.domain.account.api;

import com.sju18.petmanagement.domain.account.application.TempPasswordService;
import com.sju18.petmanagement.domain.account.dao.Account;
import com.sju18.petmanagement.domain.account.dao.AccountRepository;
import com.sju18.petmanagement.domain.account.dto.FindUsernameRequestDto;
import com.sju18.petmanagement.domain.account.dto.FindUsernameResponseDto;
import com.sju18.petmanagement.domain.account.dto.FindPasswordRequestDto;
import com.sju18.petmanagement.domain.account.dto.FindPasswordResponseDto;
import com.sju18.petmanagement.domain.account.exception.EmailNotFoundException;
import com.sju18.petmanagement.domain.account.exception.EmailVerificationFailureException;
import com.sju18.petmanagement.global.util.email.EmailService;

import com.sju18.petmanagement.global.util.email.EmailVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class FindAccountController {
    private final EmailService emailService;
    private final TempPasswordService tempPasswordService;

    final AccountRepository accountRepository;
    final PasswordEncoder encode;

    @PostMapping("/api/account/findusername")
    public ResponseEntity<?> findUsername(@RequestBody FindUsernameRequestDto findUsernameRequestDto) {
        // 이메일을 통해 계정 세부정보 조회 및 유저네임 반환
        Account foundAccount;
        try {
            foundAccount = accountRepository.findByEmail(findUsernameRequestDto.getEmail())
                    .orElseThrow(() -> new EmailNotFoundException(findUsernameRequestDto.getEmail()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new FindUsernameResponseDto("null", e.getMessage()));
        }
        return ResponseEntity.ok(new FindUsernameResponseDto(
                foundAccount.getUsername(),
                "Username Found"
        ));
    }

    @PostMapping("/api/account/findpassword")
    public ResponseEntity<?> findPassword(@RequestBody FindPasswordRequestDto findPasswordRequestDto) {
        // 유저네임 및 이메일 인증여부 확인 후 비밀번호 초기화
        try {
            Account foundAccount = accountRepository.findByUsername(findPasswordRequestDto.getUsername())
                    .orElseThrow(() -> new UsernameNotFoundException("Username not exists"));
            // 이메일 인증 확인
            if (EmailVerificationService.checkAuthCode(
                    foundAccount.getEmail(), findPasswordRequestDto.getCode()
            )) {
                String tempPassword = tempPasswordService.createTempPassword();
                // 임시 비밀번호 통지
                emailService.sendTempPasswordNotifyMessage(foundAccount.getEmail(),tempPassword);
                // 임시 비밀번호 적용
                foundAccount.setPassword(encode.encode(tempPassword));
                accountRepository.save(foundAccount);
            } else {
                throw new EmailVerificationFailureException(foundAccount.getEmail());
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new FindPasswordResponseDto(e.getMessage()));
        }
        return ResponseEntity.ok(new FindPasswordResponseDto("Temporary password notify email sent"));
    }
}
