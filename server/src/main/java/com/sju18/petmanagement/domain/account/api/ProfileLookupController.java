package com.sju18.petmanagement.domain.account.api;

import com.sju18.petmanagement.domain.account.dao.Account;
import com.sju18.petmanagement.domain.account.dao.AccountRepository;
import com.sju18.petmanagement.domain.account.dto.ProfileLookupRequestDTO;
import com.sju18.petmanagement.domain.account.dto.ProfileLookupResponseDTO;
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
public class ProfileLookupController {
    private static final Logger logger = LogManager.getLogger();

    final AccountRepository accountRepository;

    @PostMapping("/api/account/profilelookup")
    public ResponseEntity<?> lookupAccountProfile(Authentication authentication, @RequestBody ProfileLookupRequestDTO profilelookupRequestDTO) {
        // 로그인된 현재 사용자 정보 조회
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String currentUserName = userDetails.getUsername();

        // 해당 사용자의 세부정보 조회 및 반환
        try {
            Account currentUserProfile = accountRepository.findByUsername(currentUserName)
                    .orElseThrow(() -> new UsernameNotFoundException(currentUserName));
            return ResponseEntity.ok(new ProfileLookupResponseDTO(
                    "Account profile lookup success",
                    currentUserProfile.getUsername(),
                    currentUserProfile.getEmail(),
                    currentUserProfile.getName(),
                    currentUserProfile.getPhone(),
                    currentUserProfile.getPhoto()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ProfileLookupResponseDTO(e.getMessage()));
        }
    }
}
