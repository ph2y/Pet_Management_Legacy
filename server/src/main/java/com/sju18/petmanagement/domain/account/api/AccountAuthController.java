package com.sju18.petmanagement.domain.account.api;

import com.sju18.petmanagement.domain.account.application.AccountAuthService;
import com.sju18.petmanagement.domain.account.dao.Account;
import com.sju18.petmanagement.domain.account.dto.*;
import com.sju18.petmanagement.global.common.DtoMetadata;
import com.sju18.petmanagement.global.security.JwtTokenUtil;

import com.sju18.petmanagement.global.email.EmailService;
import com.sju18.petmanagement.global.email.EmailVerifyService;
import com.sju18.petmanagement.global.message.MessageConfig;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Locale;

@RestController
@AllArgsConstructor
public class AccountAuthController {
    private static final Logger logger = LogManager.getLogger();
    private final MessageSource msgSrc = MessageConfig.getAccountMessageSource();
    private final AccountAuthService accountAuthServ;
    private final EmailService emailServ;
    private final EmailVerifyService emailVerifyServ;
    private final JwtTokenUtil tokenUtil;

    @PostMapping("/api/account/login")
    public ResponseEntity<?> loginAccount(@RequestBody LoginReqDto reqDto) {
        DtoMetadata dtoMetadata;
        final Account account = accountAuthServ.loginByCredential(reqDto.getUsername(), reqDto.getPassword());
        final String token = tokenUtil.generateToken(account.getUsername());
        dtoMetadata = new DtoMetadata(msgSrc.getMessage("res.login.success", null, Locale.ENGLISH));
        return ResponseEntity.ok(new LoginResDto(dtoMetadata, token));
    }

    @PostMapping("/api/account/authcode/send")
    public ResponseEntity<?> sendAuthCodeToEmail(@RequestBody @Valid SendAuthCodeReqDto reqDto) {
        DtoMetadata dtoMetadata;
        try {
            emailServ.sendVerificationMessage(reqDto.getEmail());
        } catch (Exception e) {
            logger.warn(e.toString());
            dtoMetadata = new DtoMetadata(
                    msgSrc.getMessage("error.authcode.send.fail",
                            null, Locale.ENGLISH),
                    e.getClass().getName()
            );
            return ResponseEntity.status(400).body(new SendAuthCodeResDto(dtoMetadata));
        }
        dtoMetadata = new DtoMetadata(msgSrc.getMessage("res.authcode.send.success", null, Locale.ENGLISH));
        return ResponseEntity.ok(new SendAuthCodeResDto(dtoMetadata));
    }

    @PostMapping("/api/account/authcode/verify")
    public ResponseEntity<?> verifyAuthCode(@RequestBody VerifyAuthCodeReqDto reqDto) {
        DtoMetadata dtoMetadata;
        if(emailVerifyServ.checkAuthCode(reqDto.getEmail(), reqDto.getCode())) {
            dtoMetadata = new DtoMetadata(msgSrc.getMessage("res.authcode.verify.success", null, Locale.ENGLISH));
            return ResponseEntity.ok(new VerifyAuthCodeResDto(dtoMetadata));
        } else {
            dtoMetadata = new DtoMetadata(
                    false,
                    msgSrc.getMessage("res.authcode.verify.denied",null, Locale.ENGLISH),
                    null);
            return ResponseEntity.status(403).body(new VerifyAuthCodeResDto(dtoMetadata));
        }
    }
}
