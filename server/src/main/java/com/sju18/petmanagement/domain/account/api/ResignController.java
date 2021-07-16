package com.sju18.petmanagement.domain.account.api;

import com.sju18.petmanagement.domain.account.dao.AccountRepository;
import com.sju18.petmanagement.domain.account.dto.ResignRequestDto;
import com.sju18.petmanagement.domain.account.dto.ResignResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ResignController {
    final AccountRepository accountRepository;

    @PostMapping("/api/account/resign")
    public ResponseEntity<?> deleteAccount(Authentication authentication, @RequestBody ResignRequestDto resignRequestDto) {
        // 로그인된 현재 사용자 정보 조회
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String currentUserName = userDetails.getUsername();

        // DB에서 해당 Account 삭제
        try {
            accountRepository.deleteByUsername(currentUserName);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ResignResponseDto(e.getMessage()));
        }

        return ResponseEntity.ok(new ResignResponseDto("Account delete success"));
    }
}
