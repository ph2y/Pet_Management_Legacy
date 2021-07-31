package com.sju18.petmanagement.domain.pet.application;

import com.sju18.petmanagement.domain.account.application.AccountProfileService;
import com.sju18.petmanagement.domain.pet.dao.Pet;
import com.sju18.petmanagement.domain.pet.dao.PetRepository;
import com.sju18.petmanagement.domain.pet.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class PetProfileService {
    private final PetRepository petRepository;
    private final AccountProfileService accountProfileServ;

    // CREATE
    @Transactional
    public void createPet(Authentication auth, PetCreateReqDto reqDto) {
        String ownername = accountProfileServ.fetchCurrentAccount(auth).getUsername();

        // 받은 사용자 정보와 새 입력 정보로 새 반려동물 정보 생성
        Pet pet = Pet.builder()
                .ownername(ownername)
                .name(reqDto.getName())
                .species(reqDto.getSpecies())
                .breed(reqDto.getBreed())
                .birth(LocalDate.parse(reqDto.getBirth()))
                .yearOnly(reqDto.getYearOnly())
                .gender(reqDto.getGender())
                .message(reqDto.getMessage())
                .build();

        // save(id가 없는 transient 상태의 객체) -> EntityManger.persist() => save
        petRepository.save(pet);
    }

    // READ
    @Transactional(readOnly = true)
    public List<PetFetchResponseDto> fetchPet(Authentication auth) {
        String ownername = accountProfileServ.fetchCurrentAccount(auth).getUsername();

        // 사용자 정보로 반려동물 리스트 인출
        return petRepository.findAllByUsername(ownername).stream()
                .map(PetFetchResponseDto::new)
                .collect(Collectors.toList());
    }

    // UPDATE
    @Transactional
    public PetUpdateResponseDto updatePet(Authentication auth, PetUpdateRequestDto reqDto) {
        String ownername = accountProfileServ.fetchCurrentAccount(auth).getUsername();

        // 받은 사용자 정보와 입력 정보로 반려동물 정보 업데이트
        try {
            Pet pet = petRepository.findByUsernameAndId(ownername,reqDto.getId()).orElseThrow(() -> new IllegalArgumentException("Pet entity does not exists"));

            pet.setName(reqDto.getName());
            pet.setSpecies(reqDto.getSpecies());
            pet.setBreed(reqDto.getBreed());
            pet.setBirth(LocalDate.parse(reqDto.getBirth()));
            pet.setYearOnly(reqDto.getYear_only());
            pet.setGender(reqDto.getGender());
            pet.setMessage(reqDto.getMessage());
            pet.setPhotoUrl(reqDto.getPhoto_url());

            petRepository.save(pet); // save(id가 있는 detached 상태의 객체) -> EntityManger.merge() => update

            return new PetUpdateResponseDto("Pet profile update success");
        } catch (Exception e) {
            return new PetUpdateResponseDto(e.getMessage());
        }
    }

    // DELETE
    @Transactional
    public PetDeleteResponseDto deletePet(Authentication auth, PetDeleteReqDto reqDto) {
        String ownername = accountProfileServ.fetchCurrentAccount(auth).getUsername();

        // 받은 사용자 정보와 반려동물 id로 반려동물 정보 삭제
        try {
            Pet pet = petRepository.findByUsernameAndId(ownername,reqDto.getId()).orElseThrow(() -> new IllegalArgumentException("Pet entity does not exists"));
            petRepository.delete(pet);

            return new PetDeleteResponseDto("Pet profile delete success");
        } catch (Exception e) {
            return new PetDeleteResponseDto(e.getMessage());
        }
    }
}