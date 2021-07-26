package com.sju18.petmanagement.domain.pet.application;

import com.sju18.petmanagement.domain.pet.dao.Pet;
import com.sju18.petmanagement.domain.pet.dao.PetRepository;
import com.sju18.petmanagement.domain.pet.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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
            Pet pet = Pet.builder()
                    .username(username)
                    .name(requestDto.getName())
                    .species(requestDto.getSpecies())
                    .breed(requestDto.getBreed())
                    .birth(LocalDate.parse(requestDto.getBirth()))
                    .gender(requestDto.getGender())
                    .message(requestDto.getMessage())
                    .photo_url(requestDto.getPhoto_url())
                    .build();

            petRepository.save(pet); // save(id가 없는 transient 상태의 객체) -> EntityManger.persist() => save

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

            pet.setName(requestDto.getName());
            pet.setSpecies(requestDto.getSpecies());
            pet.setBreed(requestDto.getBreed());
            pet.setBirth(LocalDate.parse(requestDto.getBirth()));
            pet.setGender(requestDto.getGender());
            pet.setMessage(requestDto.getMessage());
            pet.setPhoto_url(requestDto.getPhoto_url());

            petRepository.save(pet); // save(id가 있는 detached 상태의 객체) -> EntityManger.merge() => update

            return new PetProfileUpdateResponseDto("Pet profile update success");
        } catch (Exception e) {
            return new PetProfileUpdateResponseDto(e.getMessage());
        }
    }

    // DELETE
    @Transactional
    public PetProfileDeleteResponseDto deletePetProfile(Authentication authentication, PetProfileDeleteRequestDto requestDto) {
        String username = getUserNameFromToken(authentication);

        // 받은 사용자 정보와 반려동물 id로 반려동물 정보 삭제
        try {
            Pet pet = petRepository.findByUsernameAndId(username,requestDto.getId()).orElseThrow(() -> new IllegalArgumentException("Pet entity does not exists"));
            petRepository.delete(pet);

            return new PetProfileDeleteResponseDto("Pet profile delete success");
        } catch (Exception e) {
            return new PetProfileDeleteResponseDto(e.getMessage());
        }
    }
}