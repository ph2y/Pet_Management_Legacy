package com.sju18.petmanagement.domain.pet.application;

import com.sju18.petmanagement.domain.pet.dao.PetFeedSchedule;
import com.sju18.petmanagement.domain.pet.dao.PetFeedScheduleRepository;
import com.sju18.petmanagement.domain.pet.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class PetFeedScheduleService {
    private final PetFeedScheduleRepository petFeedScheduleRepository;

    String getUserNameFromToken(Authentication authentication) {
        // 현재 사용자 정보 조회
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return userDetails.getUsername();
    }

    // CREATE
    @Transactional
    public PetFeedScheduleCreateResponseDto createPetFeedSchedule(Authentication authentication, PetFeedScheduleCreateRequestDto requestDto) {
        String username = getUserNameFromToken(authentication);

        // 받은 사용자 정보와 새 입력 정보로 새 반려동물 사료 시간(스케줄) 정보 생성
        try {
            PetFeedSchedule petFeedSchedule = PetFeedSchedule.builder()
                    .username(username)
                    .pet_id_list(requestDto.getPet_id_list())
                    .feed_time(LocalTime.parse(requestDto.getFeed_time()))
                    .memo(requestDto.getMemo())
                    .is_turned_on(false)
                    .build();

            petFeedScheduleRepository.save(petFeedSchedule); // save(id가 없는 transient 상태의 객체) -> EntityManger.persist() => save

            return new PetFeedScheduleCreateResponseDto("Pet feed schedule Create Success");
        } catch (Exception e) {
            return new PetFeedScheduleCreateResponseDto(e.getMessage());
        }
    }

    // READ
    @Transactional(readOnly = true)
    public List<PetFeedScheduleFetchResponseDto> fetchPetFeedSchedule(Authentication authentication) {
        String username = getUserNameFromToken(authentication);

        // 사용자 정보로 반려동물 사료 시간(스케줄) 정보 리스트 인출
        return petFeedScheduleRepository.findAllByUsername(username).stream()
                .map(PetFeedScheduleFetchResponseDto::new)
                .collect(Collectors.toList());
    }

    // UPDATE
    @Transactional
    public PetFeedScheduleUpdateResponseDto updatePetFeedSchedule(Authentication authentication, PetFeedScheduleUpdateRequestDto requestDto) {
        String username = getUserNameFromToken(authentication);

        // 받은 사용자 정보와 입력 정보로 반려동물 사료 시간(스케줄) 정보 업데이트
        try {
            PetFeedSchedule petFeedSchedule = petFeedScheduleRepository.findByUsernameAndId(username,requestDto.getId()).orElseThrow(() -> new IllegalArgumentException("PetFeedSchedule entity does not exists"));

            petFeedSchedule.setUsername(username);
            petFeedSchedule.setPet_id_list(requestDto.getPet_id_list());
            petFeedSchedule.setFeed_time(LocalTime.parse(requestDto.getFeed_time()));
            petFeedSchedule.setMemo(requestDto.getMemo());
            petFeedSchedule.setIs_turned_on(requestDto.getIs_turned_on());

            petFeedScheduleRepository.save(petFeedSchedule); // save(id가 있는 detached 상태의 객체) -> EntityManger.merge() => update

            return new PetFeedScheduleUpdateResponseDto("Pet feed schedule update success");
        } catch (Exception e) {
            return new PetFeedScheduleUpdateResponseDto(e.getMessage());
        }
    }

    // DELETE
    @Transactional
    public PetFeedScheduleDeleteResponseDto deletePetFeedSchedule(Authentication authentication, PetFeedScheduleDeleteRequestDto requestDto) {
        String username = getUserNameFromToken(authentication);

        // 받은 사용자 정보와 반려동물 id로 반려동물 사료 시간(스케줄) 정보 삭제
        try {
            PetFeedSchedule petFeedSchedule = petFeedScheduleRepository.findByUsernameAndId(username,requestDto.getId()).orElseThrow(() -> new IllegalArgumentException("PetFeedSchedule entity does not exists"));
            petFeedScheduleRepository.delete(petFeedSchedule);

            return new PetFeedScheduleDeleteResponseDto("Pet feed schedule delete success");
        } catch (Exception e) {
            return new PetFeedScheduleDeleteResponseDto(e.getMessage());
        }
    }
}
