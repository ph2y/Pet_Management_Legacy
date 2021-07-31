package com.sju18.petmanagement.domain.account.api;

import com.sju18.petmanagement.domain.account.application.AccountProfileService;
import com.sju18.petmanagement.domain.account.application.AccountLoginService;
import com.sju18.petmanagement.domain.account.dao.Account;
import com.sju18.petmanagement.domain.account.dto.*;
import com.sju18.petmanagement.global.common.DtoMetadata;
import com.sju18.petmanagement.global.config.security.JwtTokenUtil;
import com.sju18.petmanagement.global.util.message.MessageConfig;

import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.validation.Valid;
import java.util.Locale;

@RestController
@AllArgsConstructor
public class AccountController {
    private static final Logger logger = LogManager.getLogger();
    private final MessageSource msgSrc = MessageConfig.getAccountMessageSource();
    private final AccountProfileService accountServ;
    private AccountLoginService accountLoginServ;
    private JwtTokenUtil tokenUtil;

    @PostMapping("/api/account/create")
    public ResponseEntity<?> createAccount(@Valid @RequestBody CreateAccountReqDto reqDto) {
        DtoMetadata dtoMetadata;
        try {
            accountServ.createAccount(reqDto);
        } catch (Exception e) {
            logger.warn(e.toString());
            dtoMetadata = new DtoMetadata(e.getMessage(), e.getClass().getName());
            return ResponseEntity.status(400).body(new CreateAccountResDto(dtoMetadata));
        }
        dtoMetadata = new DtoMetadata(msgSrc.getMessage("res.create.success", null, Locale.ENGLISH));
        return ResponseEntity.ok(new CreateAccountResDto(dtoMetadata));
    }

    @PostMapping("/api/account/update")
    public ResponseEntity<?> updateAccount(Authentication auth, @Valid @RequestBody UpdateAccountReqDto reqDto) {
        DtoMetadata dtoMetadata;
        try {
            accountServ.updateAccount(auth, reqDto);
        } catch (Exception e) {
            logger.warn(e.toString());
            dtoMetadata = new DtoMetadata(e.getMessage(), e.getClass().getName());
            return ResponseEntity.status(400).body(new UpdateAccountResDto(dtoMetadata));
        }
        dtoMetadata = new DtoMetadata(msgSrc.getMessage("res.update.success", null, Locale.ENGLISH));
        return ResponseEntity.ok(new UpdateAccountResDto(dtoMetadata));
    }

    @PostMapping("/api/account/updatephoto")
    public ResponseEntity<?> updateAccountPhoto(Authentication auth, MultipartHttpServletRequest fileReq) {
        DtoMetadata dtoMetadata;
        String fileUrl;
        try {
            fileUrl = accountServ.updateAccountPhoto(auth, fileReq);
        } catch (Exception e) {
            logger.warn(e.toString());
            dtoMetadata = new DtoMetadata(e.getMessage(), e.getClass().getName());
            return ResponseEntity.status(400).body(new UpdateAccountPhotoResDto(dtoMetadata, null));
        }
        dtoMetadata = new DtoMetadata(msgSrc.getMessage("res.updatePhoto.success", null, Locale.ENGLISH));
        return ResponseEntity.ok(new UpdateAccountPhotoResDto(dtoMetadata, fileUrl));
    }

    @PostMapping("/api/account/fetch")
    public ResponseEntity<?> fetchAccount(Authentication auth, @Valid @RequestBody FetchAccountReqDto reqDto) {
        DtoMetadata dtoMetadata;
        final Account account;
        try {
            if (reqDto.getId() != null) {
                // 해당 id를 가진 계정 정보 조회
                account = accountServ.fetchAccountById(reqDto.getId());
            } else if (reqDto.getUsername() != null && !reqDto.getUsername().isEmpty()) {
                // 해당 username 가진 계정 정보 조회
                account = accountServ.fetchAccountByUsername(reqDto.getUsername());
            } else {
                // 현재 로그인된 계정 정보 조회
                account = accountServ.fetchCurrentAccount(auth);
            }
        } catch (Exception e) {
            logger.warn(e.toString());
            dtoMetadata = new DtoMetadata(e.getMessage(), e.getClass().getName());
            return ResponseEntity.status(400).body(new FetchAccountResDto(dtoMetadata));
        }
        dtoMetadata = new DtoMetadata(msgSrc.getMessage("res.fetch.success", null, Locale.ENGLISH));
        return ResponseEntity.ok(new FetchAccountResDto(dtoMetadata, account));
    }

    @GetMapping("/api/account/fetchphoto")
    public ResponseEntity<?> fetchAccountPhoto(Authentication auth) {
        DtoMetadata dtoMetadata;
        byte[] fileBinData;
        try {
            fileBinData = accountServ.fetchAccountPhoto(auth);
        } catch (Exception e) {
            logger.warn(e.toString());
            dtoMetadata = new DtoMetadata(e.getMessage(), e.getClass().getName());
            return ResponseEntity.status(400).body(new FetchAccountPhotoResDto(dtoMetadata));
        }
        return ResponseEntity.ok(fileBinData);
    }

    @PostMapping("/api/account/login")
    public ResponseEntity<?> loginAccount(@RequestBody LoginReqDto reqDto) {
        DtoMetadata dtoMetadata;
        final Account account = accountLoginServ.loginByCredential(reqDto.getUsername(), reqDto.getPassword());
        final String token = tokenUtil.generateToken(account.getUsername());
        dtoMetadata = new DtoMetadata(msgSrc.getMessage("res.login.success", null, Locale.ENGLISH));
        return ResponseEntity.ok(new LoginResDto(dtoMetadata, token));
    }
}
