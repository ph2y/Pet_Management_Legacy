package com.sju18.petmanagement.domain.pet.application;

import com.sju18.petmanagement.domain.pet.dao.Pet;
import com.sju18.petmanagement.domain.pet.dao.PetRepository;
import com.sju18.petmanagement.domain.pet.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class PetProfileService {
    private final PetRepository petRepository;

    String getUserNameFromToken(Authentication authentication) {
        // 현재 사용자 정보 조회
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return userDetails.getUsername();
    }

    // CREATE
    @Transactional
    public PetProfileCreateResponseDto createPetProfile(Authentication authentication, PetProfileCreateRequestDto requestDto) {
        String username = getUserNameFromToken(authentication);

        // 받은 사용자 정보와 새 입력 정보로 새 반려동물 정보 생성
        try {
            petRepository.save(requestDto.toEntity(username));
            return new PetProfileCreateResponseDto("Pet Profile Create Success");
        } catch (Exception e) {
            return new PetProfileCreateResponseDto(e.getMessage());
        }
    }

    // READ
    @Transactional(readOnly = true)
    public List<PetProfileFetchResponseDto> fetchPetProfile(Authentication authentication) {
        String username = getUserNameFromToken(authentication);

        // 사용자 정보로 반려동물 리스트 인출
        return petRepository.findAllByUsername(username).stream()
                .map(PetProfileFetchResponseDto::new)
                .collect(Collectors.toList());
    }

    // UPDATE
    @Transactional
    public PetProfileUpdateResponseDto updatePetProfile(Authentication authentication, PetProfileUpdateRequestDto requestDto) {
        String username = getUserNameFromToken(authentication);

        // 받은 사용자 정보와 입력 정보로 반려동물 정보 업데이트
        try {
            Pet pet = petRepository.findByUsernameAndId(username,requestDto.getId()).orElseThrow(() -> new IllegalArgumentException("Pet entity does not exists"));
            pet.update(requestDto);

            return new PetProfileUpdateResponseDto("Pet profile update success");
        } catch (Exception e) {
            return new PetProfileUpdateResponseDto(e.getMessage());
        }
    }

    // DELETE
    @Transactional
    public PetProfileDeleteResponseDto deletePetProfile(Authentication authentication, PetProfileDeleteRequestDto requestDto) {
        String username = getUserNameFromToken(authentication);

        // 받은 사용자 정보와 반려동물 id로 정보 삭제
        try {
            Pet pet = petRepository.findByUsernameAndId(username,requestDto.getId()).orElseThrow(() -> new IllegalArgumentException("Pet entity does not exists"));
            petRepository.delete(pet);

            return new PetProfileDeleteResponseDto("Pet profile delete success");
        } catch (Exception e) {
            return new PetProfileDeleteResponseDto(e.getMessage());
        }
    }
}