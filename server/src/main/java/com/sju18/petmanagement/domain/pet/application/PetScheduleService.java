package com.sju18.petmanagement.domain.pet.application;

import com.sju18.petmanagement.domain.account.application.AccountProfileService;
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
    private final AccountProfileService accountProfileServ;
    private final PetProfileService petProfileServ;
    private final MessageSource msgSrc = MessageConfig.getPetMessageSource();

    // CREATE
    @Transactional
    public void createPetSchedule(Authentication auth, PetScheduleCreateReqDto requestDto) {
        // 받은 사용자 정보와 새 입력 정보로 새 반려동물 사료 시간(스케줄) 정보 생성
        String username = accountProfileServ.fetchCurrentAccount(auth).getUsername();
        // 스케줄을 적용할 반려동물 id 목록
        List<Long> applyPetIdList = Stream.of(requestDto.getPetIdList().split(","))
                .map(Long::valueOf).collect(Collectors.toList());
        // 스케줄을 적용할 반려동물 목록 인출
        List<Pet> applyPetList = new ArrayList<>();
        for (Long petId : applyPetIdList) {
            applyPetList.add(petProfileServ.fetchPetById(petId));
        }
        
        // 스케줄 객체 생성
        PetSchedule petFeedSchedule = PetSchedule.builder()
                .username(username)
                .petList(applyPetList)
                .time(LocalTime.parse(requestDto.getTime()))
                .memo(requestDto.getMemo())
                .enable(false)
                .build();

        // save(id가 없는 transient 상태의 객체) -> EntityManger.persist() => save
        petScheduleRepository.save(petFeedSchedule);
    }

    // READ
    @Transactional(readOnly = true)
    public List<PetSchedule> fetchPetScheduleList(Authentication auth) {
        String username = accountProfileServ.fetchCurrentAccount(auth).getUsername();
        List<PetSchedule> petScheduleList = new ArrayList<>(petScheduleRepository.findAllByUsername(username));

        // toString 변환된 PetIdList 산출 및 제공
        for (PetSchedule petSchedule : petScheduleList) {
            List<Pet> applyPetList = petSchedule.getPetList();
            List<Long> applyPetIdList = new ArrayList<>();
            for (Pet applyPet : applyPetList) {
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
    public PetScheduleUpdateResDto updatePetSchedule(Authentication auth, PetScheduleUpdateReqDto requestDto) {
        String username = accountProfileServ.fetchCurrentAccount(auth).getUsername();
        List<Pet> ownerPetList = petProfileServ.fetchPetList(auth);

        // 받은 사용자 정보와 입력 정보로 반려동물 사료 시간(스케줄) 정보 업데이트
        try {
            PetSchedule petFeedSchedule = petScheduleRepository.findByUsernameAndId(username,requestDto.getId()).orElseThrow(() -> new IllegalArgumentException("PetSchedule entity does not exists"));

            petFeedSchedule.setUsername(username);
            petFeedSchedule.setPetList(ownerPetList);
            petFeedSchedule.setTime(LocalTime.parse(requestDto.getFeed_time()));
            petFeedSchedule.setMemo(requestDto.getMemo());
            petFeedSchedule.setEnable(requestDto.getIs_turned_on());

            petScheduleRepository.save(petFeedSchedule); // save(id가 있는 detached 상태의 객체) -> EntityManger.merge() => update

            return new PetScheduleUpdateResDto("Pet feed schedule update success");
        } catch (Exception e) {
            return new PetScheduleUpdateResDto(e.getMessage());
        }
    }

    // DELETE
    @Transactional
    public PetScheduleDeleteResDto deletePetSchedule(Authentication auth, PetScheduleDeleteReqDto requestDto) {
        String username = accountProfileServ.fetchCurrentAccount(auth).getUsername();

        // 받은 사용자 정보와 반려동물 id로 반려동물 사료 시간(스케줄) 정보 삭제
        try {
            PetSchedule petFeedSchedule = petScheduleRepository.findByUsernameAndId(username,requestDto.getId()).orElseThrow(() -> new IllegalArgumentException("PetSchedule entity does not exists"));
            petScheduleRepository.delete(petFeedSchedule);

            return new PetScheduleDeleteResDto("Pet feed schedule delete success");
        } catch (Exception e) {
            return new PetScheduleDeleteResDto(e.getMessage());
        }
    }
}
