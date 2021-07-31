package com.sju18.petmanagement.domain.account.api;

import com.sju18.petmanagement.domain.account.dao.Account;
import com.sju18.petmanagement.domain.account.dao.AccountRepository;
import com.sju18.petmanagement.domain.account.dto.UpdateAccountResDto;
import com.sju18.petmanagement.domain.account.dto.UpdateAccountReqDto;
import com.sju18.petmanagement.domain.account.dto.UploadProfilePhotoResponseDto;
import com.sju18.petmanagement.global.util.storage.FileService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@RestController
@RequiredArgsConstructor
public class ProfileUpdateController {
    private static final Logger logger = LogManager.getLogger();

    final AccountRepository accountRepository;
    final FileService fileService;

    @PostMapping("/api/account/uploadprofilephoto")
    public ResponseEntity<?> uploadProfilePhoto(Authentication authentication, MultipartHttpServletRequest fileRequest) {
        // 유저 ID 인출
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String currentUserName = userDetails.getUsername();
        Account currentUserProfile;
        String fileUrl;
        try {
            currentUserProfile = accountRepository.findByUsername(currentUserName)
                    .orElseThrow(() -> new UsernameNotFoundException(currentUserName));

            // 첨부파일 인출
            MultipartFile uploadedFile = fileRequest.getFile("file");

            // 해당 유저의 계정 스토리지에 프로필 사진 저장
            fileUrl = fileService.saveAccountProfilePhoto(currentUserProfile.getId() ,uploadedFile);

            // DB 데이터 업데이트
            currentUserProfile.setPhotoUrl(fileUrl);
            accountRepository.save(currentUserProfile);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new UploadProfilePhotoResponseDto("null", e.getMessage()));
        }
        return ResponseEntity.ok(
                new UploadProfilePhotoResponseDto(fileUrl, "Profile Photo successfully uploaded")
        );
    }
}
