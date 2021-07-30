package com.sju18.petmanagement.domain.account.application;

import com.sju18.petmanagement.domain.account.dao.Account;
import com.sju18.petmanagement.domain.account.dao.AccountRepository;
import com.sju18.petmanagement.domain.account.dto.CreateAccountReqDto;
import com.sju18.petmanagement.global.exception.DtoValidityException;
import com.sju18.petmanagement.global.util.message.MessageConfig;
import com.sju18.petmanagement.global.util.storage.FileService;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@RequiredArgsConstructor
@Service
public class AccountService {
    private final MessageSource msgSrc = MessageConfig.getAccountMessageSource();
    private final AccountRepository accountRepository;
    private final PasswordEncoder pwEncoder;
    private final FileService fileService;

    @Transactional
    public void createAccount(CreateAccountReqDto reqDto) throws Exception {
        // 중복 확인
        this.checkDuplication(reqDto);

        // Account 객체 생성
        Account newAccount = Account.builder()
                .username(reqDto.getUsername())
                .password(pwEncoder.encode(reqDto.getPassword()))
                .email(reqDto.getEmail())
                .phone(reqDto.getPhone())
                .marketing(reqDto.getMarketing())
                .nickname(reqDto.getNickname())
                .userMessage(reqDto.getUserMessage())
                .build();

        // DB에 계정정보 저장
        accountRepository.save(newAccount);

        // 프로필 파일 저장소 생성
        fileService.createAccountFileStorage(newAccount.getId());
    }

    private void checkDuplication(CreateAccountReqDto reqDto) throws DtoValidityException {
        // 중복 확인
        if (accountRepository.existsByEmail(reqDto.getEmail())) {
            throw new DtoValidityException(
                    msgSrc.getMessage("error.dup.email",null, Locale.ENGLISH)
            );
        }
        if (accountRepository.existsByUsername(reqDto.getUsername())) {
            throw new DtoValidityException(
                    msgSrc.getMessage("error.dup.username",null,Locale.ENGLISH)
            );
        }
        if (accountRepository.existsByPhone(reqDto.getPhone())) {
            throw new DtoValidityException(
                    msgSrc.getMessage("error.dup.phone",null,Locale.ENGLISH)
            );
        }
    }

}
