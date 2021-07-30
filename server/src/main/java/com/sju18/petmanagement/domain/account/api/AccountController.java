package com.sju18.petmanagement.domain.account.api;

import com.sju18.petmanagement.domain.account.application.AccountService;
import com.sju18.petmanagement.domain.account.dto.CreateAccountReqDto;
import com.sju18.petmanagement.domain.account.dto.CreateAccountResDto;
import com.sju18.petmanagement.global.common.DtoMetadata;
import com.sju18.petmanagement.global.exception.DtoMarshalException;
import com.sju18.petmanagement.global.exception.DtoValidityException;
import com.sju18.petmanagement.global.util.message.MessageService;
import com.sju18.petmanagement.global.util.storage.FileService;
import lombok.RequiredArgsConstructor;

import com.sju18.petmanagement.domain.account.dao.AccountRepository;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;

@RestController
@RequiredArgsConstructor
public class AccountController {
    private static final Logger logger = LogManager.getLogger();
    private final MessageSource msgSrc = MessageService.getAccountMessageSource();
    private final AccountService accountServ;


    @PostMapping("/api/account/signup")
    public ResponseEntity<?> registerAccount(@Validated @RequestBody CreateAccountReqDto reqDto) {
        DtoMetadata dtoMetadata;
        try {
            accountServ.createAccount(reqDto);
            dtoMetadata = new DtoMetadata(
                    true, 0,
                    msgSrc.getMessage("res.create.success", null, Locale.ENGLISH),
                    null);
            return ResponseEntity.ok(new CreateAccountResDto(dtoMetadata));
        } catch (DtoMarshalException e) {

        } catch (DtoValidityException e) {

        } catch (Exception e) {
            logger.warn(e.getMessage());
            return ResponseEntity.status(500).body(new CreateAccountResDto(e.getMessage()));
        }
    }

}


