package com.sju18.petmanagement.domain.account.application;

import com.sju18.petmanagement.domain.account.dao.Account;
import com.sju18.petmanagement.domain.account.dao.AccountRepository;
import com.sju18.petmanagement.domain.account.dto.CreateAccountReqDto;
import com.sju18.petmanagement.domain.account.dto.UpdateAccountReqDto;
import com.sju18.petmanagement.global.exception.DtoValidityException;
import com.sju18.petmanagement.global.util.message.MessageConfig;
import com.sju18.petmanagement.global.util.storage.FileService;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

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
        this.checkDuplication(reqDto.getUsername(), reqDto.getEmail(), reqDto.getPhone());

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

    private void checkDuplication(String username, String email, String phone) throws DtoValidityException {
        // 중복 확인
        if (username != null && accountRepository.existsByUsername(username)) {
            throw new DtoValidityException(
                    msgSrc.getMessage("error.dup.username",null,Locale.ENGLISH)
            );
        }
        if (email != null && accountRepository.existsByEmail(email)) {
            throw new DtoValidityException(
                    msgSrc.getMessage("error.dup.email",null, Locale.ENGLISH)
            );
        }
        if (phone != null && accountRepository.existsByPhone(phone)) {
            throw new DtoValidityException(
                    msgSrc.getMessage("error.dup.phone",null,Locale.ENGLISH)
            );
        }
    }

    @Transactional
    public void updateAccount(Authentication auth, UpdateAccountReqDto reqDto) throws Exception {
        // 기존 사용자 프로필 로드
        Account currentAccount = this.fetchCurrentAccount(auth);

        // 중복 확인
        this.checkDuplication(null, reqDto.getEmail(), reqDto.getPhone());

        // 기존 사용자 프로필 중 변경사항이 있는 필드 업데이트
        if (reqDto.getEmail() != null) {
            currentAccount.setEmail(reqDto.getEmail());
        }
        if (reqDto.getPhone() != null) {
            currentAccount.setPhone(reqDto.getPhone());
        }
        if (reqDto.getMarketing() != null) {
            currentAccount.setMarketing(reqDto.getMarketing());
        }
        if (reqDto.getNickname() != null) {
            currentAccount.setNickname(reqDto.getNickname());
        }
        if (reqDto.getUserMessage() != null) {
            currentAccount.setUserMessage(reqDto.getUserMessage());
        }

        // 기존 사용자 정보 변경사항 적용
        accountRepository.save(currentAccount);
    }

    @Transactional
    public String updateAccountPhoto(Authentication auth, MultipartHttpServletRequest fileReq) throws Exception {
        // 기존 사용자 프로필 로드
        Account currentAccount = this.fetchCurrentAccount(auth);

        // 첨부파일 인출
        MultipartFile uploadedFile = fileReq.getFile("file");

        // 해당 유저의 계정 스토리지에 프로필 사진 저장
        String fileUrl = fileService.saveAccountProfilePhoto(currentAccount.getId(), uploadedFile);

        // 파일정보 DB 데이터 업데이트
        currentAccount.setPhotoUrl(fileUrl);
        accountRepository.save(currentAccount);

        return fileUrl;
    }

    @Transactional
    public Account fetchCurrentAccount(Authentication auth) throws UsernameNotFoundException {
        // 로그인된 현재 사용자 정보 조회
        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        String currentUsername = userDetails.getUsername();
        return accountRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new UsernameNotFoundException(currentUsername));
    }

}
