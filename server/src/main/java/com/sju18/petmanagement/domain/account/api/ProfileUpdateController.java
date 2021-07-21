package com.sju18.petmanagement.domain.account.api;

import com.sju18.petmanagement.domain.account.dao.Account;
import com.sju18.petmanagement.domain.account.dao.AccountRepository;
import com.sju18.petmanagement.domain.account.dto.ProfileUpdateResponseDto;
import com.sju18.petmanagement.domain.account.dto.ProfileUpdateRequestDto;
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

@RestController
@RequiredArgsConstructor
public class ProfileUpdateController {
    private static final Logger logger = LogManager.getLogger();

    final AccountRepository accountRepository;

    @PostMapping("/api/account/profileupdate")
    public ResponseEntity<?> updateAccountProfile(Authentication authentication, @RequestBody ProfileUpdateRequestDto profileUpdateRequestDto) {
        // 로그인된 현재 사용자 정보 조회
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String currentUserName = userDetails.getUsername();
        Account currentUserProfile;
        try {
            // 기존 사용자 프로필
            currentUserProfile = accountRepository.findByUsername(currentUserName)
                    .orElseThrow(() -> new UsernameNotFoundException(currentUserName));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ProfileUpdateResponseDto(e.getMessage()));
        }

        // 기존 사용자 프로필 중 변경사항이 있는 필드 업데이트
        if (profileUpdateRequestDto.getEmail() != null) {
            currentUserProfile.setEmail(profileUpdateRequestDto.getEmail());
        }
        if (profileUpdateRequestDto.getNickname() != null) {
            currentUserProfile.setNickname(profileUpdateRequestDto.getNickname());
        }
        if (profileUpdateRequestDto.getPhone() != null) {
            currentUserProfile.setPhone(profileUpdateRequestDto.getPhone());
        }
        if (profileUpdateRequestDto.getUserMessage() != null) {
            currentUserProfile.setUserMessage(profileUpdateRequestDto.getUserMessage());
        }
        // 프로필 사진 변경 기능은 파일을 다루어야 함으로 장래 변경해야됨.
        if (profileUpdateRequestDto.getPhoto() != null) {
            currentUserProfile.setPhoto(profileUpdateRequestDto.getPhoto());
        }

        // 기존 사용자 정보 변경사항 적용
        try {
            accountRepository.save(currentUserProfile);
            return ResponseEntity.ok(new ProfileUpdateResponseDto("Account profile update success"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ProfileUpdateResponseDto(e.getMessage()));
        }
    }
}
