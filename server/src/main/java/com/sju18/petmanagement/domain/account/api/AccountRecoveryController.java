package com.sju18.petmanagement.domain.account.api;

import com.sju18.petmanagement.domain.account.application.AccountRecoveryService;
import com.sju18.petmanagement.domain.account.dto.RecoverUsernameReqDto;
import com.sju18.petmanagement.domain.account.dto.RecoverUsernameResDto;
import com.sju18.petmanagement.domain.account.dto.RecoverPasswordReqDto;
import com.sju18.petmanagement.domain.account.dto.RecoverPasswordResDto;
import com.sju18.petmanagement.global.common.DtoMetadata;
import com.sju18.petmanagement.global.message.MessageConfig;

import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;

@RestController
@AllArgsConstructor
public class AccountRecoveryController {
    private static final Logger logger = LogManager.getLogger();
    private final MessageSource msgSrc = MessageConfig.getAccountMessageSource();
    private final AccountRecoveryService accountRecoServ;

    @PostMapping("/api/account/recoverUsername")
    public ResponseEntity<?> recoverUsername(@RequestBody RecoverUsernameReqDto reqDto) {
        // 이메일을 통해 계정 세부정보 조회 및 유저네임 반환
        DtoMetadata dtoMetadata;
        String foundUsername;
        try {
            foundUsername = accountRecoServ.recoverUsernameByEmail(reqDto);
        } catch (Exception e) {
            logger.warn(e.toString());
            dtoMetadata = new DtoMetadata(e.getMessage(), e.getClass().getName());
            return ResponseEntity.status(400).body(new RecoverUsernameResDto(dtoMetadata, null));
        }
        dtoMetadata = new DtoMetadata(msgSrc.getMessage("res.recover.username.success", null, Locale.ENGLISH));
        return ResponseEntity.ok(new RecoverUsernameResDto(dtoMetadata, foundUsername));
    }

    @PostMapping("/api/account/recoverPassword")
    public ResponseEntity<?> recoverPassword(@RequestBody RecoverPasswordReqDto reqDto) {
        DtoMetadata dtoMetadata;
        try {
            // 유저네임 및 이메일 인증여부 확인 후 비밀번호 초기화
            accountRecoServ.recoverPasswordByVerifyEmail(reqDto);
        } catch (Exception e) {
            logger.warn(e.toString());
            dtoMetadata = new DtoMetadata(e.getMessage(), e.getClass().getName());
            return ResponseEntity.status(400).body(new RecoverPasswordResDto(dtoMetadata));
        }
        dtoMetadata = new DtoMetadata(msgSrc.getMessage("res.recover.password.success", null, Locale.ENGLISH));
        return ResponseEntity.ok(new RecoverPasswordResDto(dtoMetadata));
    }
}
