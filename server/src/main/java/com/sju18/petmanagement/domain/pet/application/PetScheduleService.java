package com.sju18.petmanagement.domain.pet.application;

import com.sju18.petmanagement.domain.account.application.AccountProfileService;
import com.sju18.petmanagement.domain.pet.dao.Pet;
import com.sju18.petmanagement.domain.pet.dao.PetSchedule;
import com.sju18.petmanagement.domain.pet.dao.PetScheduleRepository;
import com.sju18.petmanagement.domain.pet.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Service
public class PetScheduleService {
    private final PetScheduleRepository petFeedScheduleRepository;
    private final AccountProfileService accountProfileServ;
    private final PetProfileService petProfileServ;

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
        petFeedScheduleRepository.save(petFeedSchedule);
    }

    // READ
    @Transactional(readOnly = true)
    public List<PetScheduleFetchResDto> fetchPetSchedule(Authentication auth) {
        String username = accountProfileServ.fetchCurrentAccount(auth).getUsername();

        // 사용자 정보로 반려동물 사료 시간(스케줄) 정보 리스트 인출
        return petFeedScheduleRepository.findAllByUsername(username).stream()
                .map(PetScheduleFetchResDto::new)
                .collect(Collectors.toList());
    }

    // UPDATE
    @Transactional
    public PetScheduleUpdateResDto updatePetSchedule(Authentication auth, PetScheduleUpdateReqDto requestDto) {
        String username = accountProfileServ.fetchCurrentAccount(auth).getUsername();
        List<Pet> ownerPetList = petProfileServ.fetchPetList(auth);

        // 받은 사용자 정보와 입력 정보로 반려동물 사료 시간(스케줄) 정보 업데이트
        try {
            PetSchedule petFeedSchedule = petFeedScheduleRepository.findByUsernameAndId(username,requestDto.getId()).orElseThrow(() -> new IllegalArgumentException("PetSchedule entity does not exists"));

            petFeedSchedule.setUsername(username);
            petFeedSchedule.setPetList(ownerPetList);
            petFeedSchedule.setTime(LocalTime.parse(requestDto.getFeed_time()));
            petFeedSchedule.setMemo(requestDto.getMemo());
            petFeedSchedule.setEnable(requestDto.getIs_turned_on());

            petFeedScheduleRepository.save(petFeedSchedule); // save(id가 있는 detached 상태의 객체) -> EntityManger.merge() => update

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
            PetSchedule petFeedSchedule = petFeedScheduleRepository.findByUsernameAndId(username,requestDto.getId()).orElseThrow(() -> new IllegalArgumentException("PetSchedule entity does not exists"));
            petFeedScheduleRepository.delete(petFeedSchedule);

            return new PetScheduleDeleteResDto("Pet feed schedule delete success");
        } catch (Exception e) {
            return new PetScheduleDeleteResDto(e.getMessage());
        }
    }
}
