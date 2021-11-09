package com.sju18.petmanagement.global.error;

import com.sju18.petmanagement.domain.account.dto.CreateAccountResDto;
import com.sju18.petmanagement.global.common.DtoMetadata;
import com.sju18.petmanagement.global.message.MessageConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;
import java.util.Objects;

@ControllerAdvice
@RestController
// 전역 예외 처리 클래스
public class ExceptionAdvisor {
    private static final Logger logger = LogManager.getLogger();
    private final MessageSource msgSrc = MessageConfig.getValidationMessageSource();

    // Spring DTO validation 예외 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> processValidationError(MethodArgumentNotValidException ex) {
        DtoMetadata dtoMetadata;
        String errMsg;
        try {
            errMsg = msgSrc.getMessage(
                    Objects.requireNonNull(ex.getAllErrors().get(0).getDefaultMessage()),
                    ex.getAllErrors().get(0).getArguments(),
                    Locale.ENGLISH
            );
        } catch (Exception e) {
            errMsg = "Error message localize failure";
        }

        // 예외 처리
        logger.warn(ex.toString());
        dtoMetadata = new DtoMetadata(errMsg, ex.getClass().getName());
        return ResponseEntity.status(400).body(new CreateAccountResDto(dtoMetadata));
    }
}
