package com.sju18.petmanagement.domain.account.api;

import com.sju18.petmanagement.domain.account.application.AccountService;
import com.sju18.petmanagement.domain.account.dao.Account;
import com.sju18.petmanagement.domain.account.dto.*;
import com.sju18.petmanagement.global.common.DtoMetadata;
import com.sju18.petmanagement.global.message.MessageConfig;

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
    private final AccountService accountServ;

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
            } else if (reqDto.getNickname() != null && !reqDto.getNickname().isEmpty()) {
                // 해당 nickname 가진 계정 정보 조회
                account = accountServ.fetchAccountByNickname(reqDto.getNickname());

                // check if self -> return exception
                Account myAccount = accountServ.fetchCurrentAccount(auth);
                if(account == myAccount) {
                    logger.warn("fetched self");
                    dtoMetadata = new DtoMetadata("fetched self");
                    return ResponseEntity.status(400).body(new FetchAccountResDto(dtoMetadata));
                }
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

    @GetMapping("/api/account/photo/fetch")
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

    @PostMapping("/api/account/password/update")
    public ResponseEntity<?> updatePasswordAccount(Authentication auth, @Valid @RequestBody UpdateAccountPasswordReqDto reqDto) {
        DtoMetadata dtoMetadata;
        try {
            accountServ.updateAccountPassword(auth, reqDto.getPassword());
        } catch (Exception e) {
            logger.warn(e.toString());
            dtoMetadata = new DtoMetadata(e.getMessage(), e.getClass().getName());
            return ResponseEntity.status(400).body(new UpdateAccountPasswordResDto(dtoMetadata));
        }
        dtoMetadata = new DtoMetadata(msgSrc.getMessage("res.password.update.success", null, Locale.ENGLISH));
        return ResponseEntity.ok(new UpdateAccountPasswordResDto(dtoMetadata));
    }

    @PostMapping("/api/account/photo/update")
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
        dtoMetadata = new DtoMetadata(msgSrc.getMessage("res.photo.update.success", null, Locale.ENGLISH));
        return ResponseEntity.ok(new UpdateAccountPhotoResDto(dtoMetadata, fileUrl));
    }

    @PostMapping("/api/account/delete")
    public ResponseEntity<?> deleteAccount(Authentication auth) {
        DtoMetadata dtoMetadata;
        try {
            accountServ.deleteAccount(auth);
        } catch (Exception e) {
            logger.warn(e.toString());
            dtoMetadata = new DtoMetadata(e.getMessage(), e.getClass().getName());
            return ResponseEntity.status(400).body(new DeleteAccountResDto(dtoMetadata));
        }
        dtoMetadata = new DtoMetadata(msgSrc.getMessage("res.delete.success", null, Locale.ENGLISH));
        return ResponseEntity.ok(new DeleteAccountResDto(dtoMetadata));
    }
}
