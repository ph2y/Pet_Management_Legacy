package com.sju18.petmanagement.domain.pet.application;

import com.sju18.petmanagement.domain.pet.dao.Pet;
import com.sju18.petmanagement.domain.pet.dao.PetRepository;
import com.sju18.petmanagement.domain.pet.dto.PetInfoResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class PetInfoService {
    private final PetRepository petRepository;

    @Transactional
    public PetInfoResponseDTO fetchPetInfo(Authentication authentication) {
        // 로그인된 현재 사용자 정보 조회
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String currentUserName = userDetails.getUsername();
        System.out.println(currentUserName);
        // 해당 사용자의 펫 세부정보 조회 및 반환
        try {
            Pet petInfo = petRepository.findByUsername(currentUserName)
                    .orElseThrow(() -> new UsernameNotFoundException(currentUserName));
            System.out.println(petInfo.getName());
            return new PetInfoResponseDTO("fetch success",petInfo.getName(), petInfo.getBirth(), petInfo.getSpecies(), petInfo.getSex());
            /* return new PetInfoResponseDTO.builder()
                    .message("fetch success")
                    .name(petInfo.getName())
                    .birth(petInfo.getBirth())
                    .species(petInfo.getSpecies())
                    .sex(petInfo.getSex())
                    .build(); */
        } catch (Exception e) {
            return new PetInfoResponseDTO(e.getMessage());
        }
    }
}
