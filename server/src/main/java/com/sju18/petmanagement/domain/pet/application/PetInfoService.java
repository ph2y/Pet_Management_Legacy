package com.sju18.petmanagement.domain.pet.application;

import com.sju18.petmanagement.domain.pet.dao.Pet;
import com.sju18.petmanagement.domain.pet.dao.PetRepository;
import com.sju18.petmanagement.domain.pet.dto.PetInfoCreateRequestDto;
import com.sju18.petmanagement.domain.pet.dto.PetInfoDeleteRequestDto;
import com.sju18.petmanagement.domain.pet.dto.PetInfoResponseDto;
import com.sju18.petmanagement.domain.pet.dto.PetInfoUpdateRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class PetInfoService {
    private final PetRepository petRepository;

    String getUserNameFromToken(Authentication authentication) {
        // 현재 사용자 정보 조회
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return userDetails.getUsername();
    }

    // CREATE
    @Transactional
    public Long createPetInfo(Authentication authentication, PetInfoCreateRequestDto requestDto) {
        String username = getUserNameFromToken(authentication);

        // 받은 사용자 정보와 새 입력 정보로 새 반려동물 정보 생성
        try {
            return petRepository.save(requestDto.toEntity(username)).getId();
        } catch (Exception e) {
            return (long) -1;
        }
    }

    // READ
    @Transactional(readOnly = true)
    public List<PetInfoResponseDto> fetchPetInfo(Authentication authentication) {
        String username = getUserNameFromToken(authentication);

        // 사용자 정보로 반려동물 리스트 인출
        return petRepository.findAllByUsername(username).stream()
                .map(PetInfoResponseDto::new)
                .collect(Collectors.toList());
    }

    // UPDATE
    @Transactional
    public Long updatePetInfo(Authentication authentication, PetInfoUpdateRequestDto requestDto) {
        String username = getUserNameFromToken(authentication);

        // 받은 사용자 정보와 입력 정보로 반려동물 정보 업데이트
        Pet pet = petRepository.findById(requestDto.getId()).orElseThrow(() -> new IllegalArgumentException("해당 id를 가진 반려동물 정보가 없습니다."));

        return pet.update(requestDto);
    }

    // DELETE
    @Transactional
    public void deletePetInfo(Authentication authentication, PetInfoDeleteRequestDto requestDto) {
        String username = getUserNameFromToken(authentication);

        // 받은 사용자 정보와 반려동물 id로 정보 삭제
        Pet pet = petRepository.findById(requestDto.getId()).orElseThrow(() -> new IllegalArgumentException("해당 id를 가진 반려동물 정보가 없습니다."));

        petRepository.delete(pet);
    }
}
