package com.sju18.petmanagement.domain.account.application;

import com.sju18.petmanagement.domain.account.dao.Account;
import com.sju18.petmanagement.domain.account.dao.AccountRepository;
import com.sju18.petmanagement.domain.account.dto.RecoverPasswordReqDto;
import com.sju18.petmanagement.domain.account.dto.RecoverUsernameReqDto;
import com.sju18.petmanagement.global.email.EmailService;
import com.sju18.petmanagement.global.email.EmailVerifyService;
import com.sju18.petmanagement.global.message.MessageConfig;
import com.sju18.petmanagement.global.tempcode.TempCodeGenerator;
import lombok.AllArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@AllArgsConstructor
public class AccountRecoveryService {
    private final AccountRepository accountRepository;
    private final EmailService emailServ;
    private final EmailVerifyService emailVerifyServ;
    private final PasswordEncoder pwEncoder;
    private final MessageSource msgSrc = MessageConfig.getAccountMessageSource();

    public String recoverUsernameByEmail(RecoverUsernameReqDto reqDto) throws Exception {
        Account foundAccount;
        Object[] args = { reqDto.getEmail() };
        foundAccount = accountRepository.findByEmail(reqDto.getEmail())
                .orElseThrow(() -> new Exception(
                        msgSrc.getMessage("error.recover.email.notExist", args, Locale.ENGLISH)
                ));
        return foundAccount.getUsername();
    }

    public void recoverPasswordByVerifyEmail(RecoverPasswordReqDto reqDto) throws Exception {
        Account foundAccount = accountRepository.findByUsername(reqDto.getUsername())
                .orElseThrow(() -> new Exception(msgSrc.getMessage("error.notExist", null, Locale.ENGLISH)));
        // 이메일 인증 확인
        if (emailVerifyServ.checkAuthCode(foundAccount.getEmail(), reqDto.getCode())) {
            String tempPassword = this.createTempPassword();
            // 임시 비밀번호 통지
            emailServ.sendTempPasswordNotifyMessage(foundAccount.getEmail(),tempPassword);
            // 임시 비밀번호 적용
            foundAccount.setPassword(pwEncoder.encode(tempPassword));
            accountRepository.save(foundAccount);
        } else {
            Object[] args = { foundAccount.getEmail() };
            throw new Exception(msgSrc.getMessage("error.recover.email.denied", args, Locale.ENGLISH));
        }
    }

    private String createTempPassword() {
        char[] tempPasswordCharSet = new char[] {
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S',
                'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
                '!','@','#','$','%','^','&'
        };
        return TempCodeGenerator.generate(tempPasswordCharSet, 8);
    }
}
