package com.sju18.petmanagement.domain.account.api;

import com.sju18.petmanagement.domain.account.application.AccountService;
import com.sju18.petmanagement.domain.account.dto.CreateAccountReqDto;
import com.sju18.petmanagement.domain.account.dto.CreateAccountResDto;
import com.sju18.petmanagement.global.common.DtoMetadata;
import com.sju18.petmanagement.global.exception.DbException;
import com.sju18.petmanagement.global.exception.DtoValidityException;
import com.sju18.petmanagement.global.exception.StorageException;
import com.sju18.petmanagement.global.util.message.MessageConfig;

import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Locale;

@RestController
@RequiredArgsConstructor
public class AccountController {
    private static final Logger logger = LogManager.getLogger();
    private final MessageSource msgSrc = MessageConfig.getAccountMessageSource();
    private final AccountService accountServ;


    @PostMapping("/api/account/create")
    public ResponseEntity<?> registerAccount(@Valid @RequestBody CreateAccountReqDto reqDto) {
        DtoMetadata dtoMetadata;
        try {
            accountServ.createAccount(reqDto);
            dtoMetadata = new DtoMetadata(
                    true, 0,
                    msgSrc.getMessage("res.create.success", null, Locale.ENGLISH),
                    null);
            return ResponseEntity.ok(new CreateAccountResDto(dtoMetadata));
        } catch (MethodArgumentNotValidException e) {
//            throw new DtoValidityException(e.getBindingResult().getAllErrors().get(0).getDefaultMessage());
        } catch (DtoValidityException e) {
            // 양식 검증 예외 처리
            logger.warn(e.toString());
            logger.debug(e.getStackTrace());
            dtoMetadata = new DtoMetadata(false, 9, e.getMessage(), e.toString());
            return ResponseEntity.status(400).body(new CreateAccountResDto(dtoMetadata));
        } catch (DbException e) {
            // DB 에러 오류 처리
            logger.warn(e.toString());
            logger.debug(e.getStackTrace());
            dtoMetadata = new DtoMetadata(false, 5, e.getMessage(), e.toString());
            return ResponseEntity.status(500).body(new CreateAccountResDto(dtoMetadata));
        } catch (StorageException e) {
            // 파일시스템 에러 오류 처리
            logger.warn(e.toString());
            logger.debug(e.getStackTrace());
            dtoMetadata = new DtoMetadata(false, 6, e.getMessage(), e.toString());
            return ResponseEntity.status(500).body(new CreateAccountResDto(dtoMetadata));
        } catch (Exception e) {
            // 처리되지 않은 예외 처리
            logger.error(e.toString());
            logger.debug(e.getStackTrace());
            dtoMetadata = new DtoMetadata(false, 9, e.getMessage(), e.toString());
            return ResponseEntity.status(400).body(new CreateAccountResDto(dtoMetadata));
        }
    }

}


