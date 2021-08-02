package com.sju18.petmanagement.domain.pet.application;

import com.sju18.petmanagement.domain.account.application.AccountService;
import com.sju18.petmanagement.domain.pet.dao.Pet;
import com.sju18.petmanagement.domain.pet.dao.PetRepository;
import com.sju18.petmanagement.domain.pet.dto.*;
import com.sju18.petmanagement.global.message.MessageConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@RequiredArgsConstructor
@Service
public class PetService {
    private final PetRepository petRepository;
    private final AccountService accountServ;
    private final MessageSource msgSrc = MessageConfig.getPetMessageSource();

    // CREATE
    @Transactional
    public void createPet(Authentication auth, PetCreateReqDto reqDto) {
        String ownername = accountServ.fetchCurrentAccount(auth).getUsername();

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
    public List<Pet> fetchPetList(Authentication auth) {
        String ownername = accountServ.fetchCurrentAccount(auth).getUsername();

        // 사용자 정보로 반려동물 리스트 인출
        return new ArrayList<>(petRepository.findAllByOwnername(ownername));
    }

    @Transactional(readOnly = true)
    public Pet fetchPetById(Long petId) {
        // 반려동물 고유번호로 반려동물 인출
        return petRepository.findById(petId)
                .orElseThrow(() -> new IllegalArgumentException(
                        msgSrc.getMessage("error.notExists", null, Locale.ENGLISH)
                ));
    }

    // UPDATE
    @Transactional
    public void updatePet(Authentication auth, PetUpdateReqDto reqDto) {
        // 받은 사용자 정보와 입력 정보로 반려동물 정보 업데이트
        String ownername = accountServ.fetchCurrentAccount(auth).getUsername();
        Pet currentPet = petRepository.findByOwnernameAndId(ownername, reqDto.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        msgSrc.getMessage("error.notExists", null, Locale.ENGLISH)
                ));

        if (reqDto.getName() != null && !reqDto.getName().isEmpty() && !reqDto.getName().equals(currentPet.getName())) {
            currentPet.setName(reqDto.getName());
        }
        if (reqDto.getSpecies() != null && !reqDto.getSpecies().equals(currentPet.getSpecies())) {
            currentPet.setSpecies(reqDto.getSpecies());
        }
        if (reqDto.getBreed() != null && !reqDto.getBreed().equals(currentPet.getBreed())) {
            currentPet.setBreed(reqDto.getBreed());
        }
        if (reqDto.getBirth() != null && !reqDto.getBirth().equals(currentPet.getBirth().toString())) {
            currentPet.setBirth(LocalDate.parse(reqDto.getBirth()));
        }
        if (reqDto.getYearOnly() != null && !reqDto.getYearOnly().equals(currentPet.getYearOnly())) {
            currentPet.setYearOnly(reqDto.getYearOnly());
        }
        if (reqDto.getGender() != null && !reqDto.getGender().equals(currentPet.getGender())) {
            currentPet.setGender(reqDto.getGender());
        }
        if (reqDto.getMessage() != null && !reqDto.getMessage().equals(currentPet.getMessage())) {
            currentPet.setMessage(reqDto.getMessage());
        }

        // save(id가 있는 detached 상태의 객체) -> EntityManger.merge() => update
        petRepository.save(currentPet);
    }

    // DELETE
    @Transactional
    public void deletePet(Authentication auth, PetDeleteReqDto reqDto) {
        // 받은 사용자 정보와 반려동물 id로 반려동물 정보 삭제
        String ownername = accountServ.fetchCurrentAccount(auth).getUsername();
        Pet pet = petRepository.findByOwnernameAndId(ownername, reqDto.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        msgSrc.getMessage("error.notExists", null, Locale.ENGLISH)
                ));
        petRepository.delete(pet);
    }
}
