package com.sju18.petmanagement.domain.account.application;

import com.sju18.petmanagement.domain.account.dao.Account;
import com.sju18.petmanagement.domain.account.dao.AccountRepository;
import com.sju18.petmanagement.domain.account.dto.CreateAccountReqDto;
import com.sju18.petmanagement.domain.account.dto.UpdateAccountReqDto;
import com.sju18.petmanagement.domain.pet.application.PetCascadeService;
import com.sju18.petmanagement.global.exception.DtoValidityException;
import com.sju18.petmanagement.global.message.MessageConfig;
import com.sju18.petmanagement.global.storage.FileService;

import lombok.AllArgsConstructor;
import org.apache.commons.io.IOUtil;
import org.springframework.context.MessageSource;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Locale;

@Service
@AllArgsConstructor
public class AccountService {
    private final MessageSource msgSrc = MessageConfig.getAccountMessageSource();
    private final AccountRepository accountRepository;
    private final PetCascadeService petCascadeServ;
    private final FileService fileServ;
    private final PasswordEncoder pwEncoder;

    @Transactional
    public void createAccount(CreateAccountReqDto reqDto) throws Exception {
        // 중복 확인
        this.checkEmailDuplication(reqDto.getEmail());
        this.checkUsernameDuplication(reqDto.getUsername());
        this.checkPhoneDuplication(reqDto.getPhone());

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
        fileServ.createAccountFileStorage(newAccount.getId());
    }

    private void checkUsernameDuplication(String username) throws Exception {
        // 닉네임 중복 확인
        if (username != null && accountRepository.existsByUsername(username)) {
            throw new DtoValidityException(
                    msgSrc.getMessage("error.dup.username",null,Locale.ENGLISH)
            );
        }
    }

    private void checkEmailDuplication(String email) throws Exception {
        // 이메일 중복 확인
        if (email != null && accountRepository.existsByEmail(email)) {
            throw new DtoValidityException(
                    msgSrc.getMessage("error.dup.email",null, Locale.ENGLISH)
            );
        }
    }

    private void checkPhoneDuplication(String phone) throws Exception {
        // 전화번호 중복 확인
        if (phone != null && accountRepository.existsByPhone(phone)) {
            throw new DtoValidityException(
                    msgSrc.getMessage("error.dup.phone",null,Locale.ENGLISH)
            );
        }
    }

    @Transactional(readOnly = true)
    public Account fetchCurrentAccount(Authentication auth) throws UsernameNotFoundException {
        // 로그인된 현재 사용자 정보 조회
        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        String currentUsername = userDetails.getUsername();
        return accountRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new UsernameNotFoundException(currentUsername));
    }

    @Transactional(readOnly = true)
    public Account fetchAccountById(Long id) throws Exception {
        // 해당 id 가진 계정 정보 조회
        return accountRepository.findById(id)
                .orElseThrow(() -> new Exception(msgSrc.getMessage("error.notExist", null, Locale.ENGLISH)));
    }

    @Transactional(readOnly = true)
    public Account fetchAccountByUsername(String username) throws Exception {
        // 해당 username 가진 계정 정보 조회
        return accountRepository.findByUsername(username)
                .orElseThrow(() -> new Exception(msgSrc.getMessage("error.notExist", null, Locale.ENGLISH)));
    }

    @Transactional(readOnly = true)
    public Account fetchAccountByNickname(Authentication auth, String nickname) throws Exception {
        // 해당 nickname 가진 계정 정보 조회
        Account account = accountRepository.findByNickname(nickname)
                .orElseThrow(() -> new Exception(msgSrc.getMessage("error.notExist", null, Locale.ENGLISH)));

        // check if self
        if(account == this.fetchCurrentAccount(auth)) {
            throw new Exception(msgSrc.getMessage("error.fetchedSelf", null, Locale.ENGLISH));
        }

        return account;
    }

    public byte[] fetchAccountPhoto(Authentication auth, Long id) throws Exception {
        Account currentAccount;

        // if id is null(-1) -> fetch self photo
        if(id == null) { currentAccount = this.fetchCurrentAccount(auth); }
        // if id is not null -> fetch id's photo
        else { currentAccount = this.fetchAccountById(id); }

        // 사진 파일 인출
        InputStream imageStream = new FileInputStream(currentAccount.getPhotoUrl());
        byte[] fileBinData = IOUtil.toByteArray(imageStream);
        imageStream.close();
        return fileBinData;
    }

    @Transactional
    public void updateAccount(Authentication auth, UpdateAccountReqDto reqDto) throws Exception {
        // 기존 사용자 프로필 로드
        Account currentAccount = this.fetchCurrentAccount(auth);

        // 기존 사용자 프로필 중 변경사항이 있는 필드 업데이트
        if (reqDto.getEmail() != null && !reqDto.getEmail().equals(currentAccount.getEmail())) {
            // 중복 확인
            this.checkEmailDuplication(reqDto.getEmail());
            currentAccount.setEmail(reqDto.getEmail());
        }
        if (reqDto.getPhone() != null && !reqDto.getPhone().equals(currentAccount.getPhone())) {
            // 중복 확인
            this.checkPhoneDuplication(reqDto.getPhone());
            currentAccount.setPhone(reqDto.getPhone());
        }
        if (reqDto.getMarketing() != null && !reqDto.getMarketing().equals(currentAccount.getMarketing())) {
            currentAccount.setMarketing(reqDto.getMarketing());
        }
        if (reqDto.getNickname() != null && !reqDto.getNickname().equals(currentAccount.getNickname())) {
            currentAccount.setNickname(reqDto.getNickname());
        }
        if (reqDto.getUserMessage() != null && !reqDto.getUserMessage().equals(currentAccount.getUserMessage())) {
            currentAccount.setUserMessage(reqDto.getUserMessage());
        }

        // 기존 사용자 정보 변경사항 적용
        accountRepository.save(currentAccount);
    }

    @Transactional
    public void updateAccountPassword(Authentication auth, String password, String newPassword) {
        // 기존 사용자 프로필 로드
        Account currentAccount = this.fetchCurrentAccount(auth);

        // 기존 비밀번호 일치 확인
        if(!pwEncoder.matches(password, currentAccount.getPassword())) {
            throw new BadCredentialsException(msgSrc.getMessage("error.password.mismatch", null, Locale.ENGLISH));
        }

        // 새로운 비밀번호가 기존 비밀번호와 다르면 업데이트
        if (!pwEncoder.encode(newPassword).equals(currentAccount.getPassword())) {
            currentAccount.setPassword(pwEncoder.encode(newPassword));
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
        String fileUrl = null;
        if (uploadedFile != null) {
            fileUrl = fileServ.saveAccountPhoto(currentAccount.getId(), uploadedFile);

            // 파일정보 DB 데이터 업데이트
            currentAccount.setPhotoUrl(fileUrl);
            accountRepository.save(currentAccount);
        }

        return fileUrl;
    }

    @Transactional
    public void deleteAccount(Authentication auth) throws Exception {
        Account currentAccount = this.fetchCurrentAccount(auth);
        fileServ.deleteAccountFileStorage(currentAccount.getId());
        petCascadeServ.deleteAccountCascadeToPet(currentAccount);
        accountRepository.deleteById(currentAccount.getId());
    }

}
