package com.sju18.petmanagement.domain.pet.application;

import com.sju18.petmanagement.domain.account.application.AccountService;
import com.sju18.petmanagement.domain.pet.dao.Pet;
import com.sju18.petmanagement.domain.pet.dao.PetSchedule;
import com.sju18.petmanagement.domain.pet.dao.PetScheduleRepository;
import com.sju18.petmanagement.domain.pet.dto.*;
import com.sju18.petmanagement.global.message.MessageConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Service
public class PetScheduleService {
    private final PetScheduleRepository petScheduleRepository;
    private final AccountService accountServ;
    private final PetService petServ;
    private final MessageSource msgSrc = MessageConfig.getPetMessageSource();

    // CREATE
    @Transactional
    public void createPetSchedule(Authentication auth, CreatePetScheduleReqDto reqDto) {
        // 받은 사용자 정보와 새 입력 정보로 새 반려동물 사료 시간(스케줄) 정보 생성
        String username = accountServ.fetchCurrentAccount(auth).getUsername();
        // 스케줄을 적용할 반려동물 id 목록 스트링을 통해 스케줄을 적용할 반려동물 객체 목록 인출
        List<Pet> appliedPetList = this.getAppliedPetListFromApplyPetIdList(reqDto.getPetIdList());
        
        // 스케줄 객체 생성
        PetSchedule petSchedule = PetSchedule.builder()
                .username(username)
                .petList(appliedPetList)
                .time(LocalTime.parse(reqDto.getTime()))
                .memo(reqDto.getMemo())
                .enabled(false)
                .build();

        // save(id가 없는 transient 상태의 객체) -> EntityManger.persist() => save
        petScheduleRepository.save(petSchedule);
    }

    private List<Pet> getAppliedPetListFromApplyPetIdList(String petIdList) {
        // 스케줄을 적용할 반려동물 id 목록
        List<Long> applyPetIdList = Stream.of(petIdList.split(","))
                .map(Long::valueOf).collect(Collectors.toList());
        // 스케줄을 적용할 반려동물 목록 인출
        List<Pet> appliedPetList = new ArrayList<>();
        for (Long petId : applyPetIdList) {
            appliedPetList.add(petServ.fetchPetById(petId));
        }
        return appliedPetList;
    }

    // READ
    @Transactional(readOnly = true)
    public List<PetSchedule> fetchPetScheduleList(Authentication auth) {
        String username = accountServ.fetchCurrentAccount(auth).getUsername();
        List<PetSchedule> petScheduleList = new ArrayList<>(petScheduleRepository.findAllByUsername(username));

        // toString 변환된 PetIdList 산출 및 제공
        for (PetSchedule petSchedule : petScheduleList) {
            List<Pet> appliedPetList = petSchedule.getPetList();
            List<Long> applyPetIdList = new ArrayList<>();
            for (Pet applyPet : appliedPetList) {
                applyPetIdList.add(applyPet.getId());
            }
            petSchedule.setPetIdList(applyPetIdList.toString()
                    .replace("[", "")
                    .replace("]", "")
            );
        }
        
        // 사용자 정보로 반려동물 사료 시간(스케줄) 정보 리스트 인출
        return petScheduleList;
    }

    @Transactional(readOnly = true)
    public PetSchedule fetchPetScheduleById(Long petScheduleId) {
        // 반려동물 스케줄 고유번호로 반려동물 스케줄 인출
        return petScheduleRepository.findById(petScheduleId)
                .orElseThrow(() -> new IllegalArgumentException(
                        msgSrc.getMessage("error.notExists", null, Locale.ENGLISH)
                ));
    }

    // UPDATE
    @Transactional
    public void updatePetSchedule(Authentication auth, UpdatePetScheduleReqDto reqDto) {
        String username = accountServ.fetchCurrentAccount(auth).getUsername();

        // 받은 사용자 정보와 입력 정보로 반려동물 사료 시간(스케줄) 정보 업데이트
        PetSchedule petSchedule = petScheduleRepository.findByUsernameAndId(username,reqDto.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        msgSrc.getMessage("error.notExists", null, Locale.ENGLISH)
                ));

        if (reqDto.getPetIdList() != null) {
            // 스케줄을 적용할 반려동물 id 목록 스트링을 통해 스케줄을 적용할 반려동물 객체 목록 인출
            List<Pet> appliedPetList = this.getAppliedPetListFromApplyPetIdList(reqDto.getPetIdList());
            petSchedule.setPetList(appliedPetList);
        }
        if (reqDto.getTime() != null && !reqDto.getTime().equals(petSchedule.getTime().toString())) {
            petSchedule.setTime(LocalTime.parse(reqDto.getTime()));
        }
        if (reqDto.getMemo() != null && !reqDto.getMemo().equals(petSchedule.getMemo())) {
            petSchedule.setMemo(reqDto.getMemo());
        }
        if (reqDto.getEnabled() != null && !reqDto.getEnabled().equals(petSchedule.getEnabled())) {
            petSchedule.setEnabled(reqDto.getEnabled());
        }

        // save(id가 있는 detached 상태의 객체) -> EntityManger.merge() => update
        petScheduleRepository.save(petSchedule);
    }

    // DELETE
    @Transactional
    public void deletePetSchedule(Authentication auth, DeletePetScheduleReqDto reqDto) {
        String username = accountServ.fetchCurrentAccount(auth).getUsername();

        // 받은 사용자 정보와 반려동물 스케줄 id로 반려동물 사료 시간(스케줄) 정보 삭제
        PetSchedule petSchedule = petScheduleRepository.findByUsernameAndId(username,reqDto.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        msgSrc.getMessage("error.notExists", null, Locale.ENGLISH))
                );
        petScheduleRepository.delete(petSchedule);
    }
}
