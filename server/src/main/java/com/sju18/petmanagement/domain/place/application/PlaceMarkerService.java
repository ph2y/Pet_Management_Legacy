package com.sju18.petmanagement.domain.place.application;

import com.sju18.petmanagement.domain.place.dao.Place;
import com.sju18.petmanagement.domain.place.dao.PlaceRepository;
import com.sju18.petmanagement.domain.place.dto.PlaceMarkerCreateRequestDto;
import com.sju18.petmanagement.domain.place.dto.PlaceMarkerDeleteRequestDto;
import com.sju18.petmanagement.domain.place.dto.PlaceMarkerResponseDto;
import com.sju18.petmanagement.domain.place.dto.PlaceMarkerUpdateRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class PlaceMarkerService {
    private final PlaceRepository placeRepository;

    String getUserNameFromToken(Authentication authentication) {
        // 현재 사용자 정보 조회
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return userDetails.getUsername();
    }

    // CREATE
    @Transactional
    public Long createPlaceMarker(Authentication authentication, PlaceMarkerCreateRequestDto requestDto) {
        String username = getUserNameFromToken(authentication);

        // 받은 사용자 정보와 새 입력 정보로 새 장소 마커 생성
        try {
            return placeRepository.save(requestDto.toEntity(username)).getId();
        } catch (Exception e) {
            return (long) -1;
        }
    }

    // READ
    @Transactional(readOnly = true)
    public List<PlaceMarkerResponseDto> fetchPlaceMarker(Authentication authentication) {
        String username = getUserNameFromToken(authentication);

        // 사용자 정보로 장소 마커 리스트 인출
        return placeRepository.findAllByUsername(username).stream()
                .map(PlaceMarkerResponseDto::new)
                .collect(Collectors.toList());
    }

    // UPDATE
    @Transactional
    public Long updatePlaceMarker(Authentication authentication, PlaceMarkerUpdateRequestDto requestDto) {
        String username = getUserNameFromToken(authentication);

        // 받은 사용자 정보와 입력 정보로 장소 마커 업데이트
        Place place = placeRepository.findById(requestDto.getId()).orElseThrow(() -> new IllegalArgumentException("해당 id를 가진 장소 마커가 없습니다."));

        return place.update(requestDto);
    }

    // DELETE
    @Transactional
    public void deletePlaceMarker(Authentication authentication, PlaceMarkerDeleteRequestDto requestDto) {
        String username = getUserNameFromToken(authentication);

        // 받은 사용자 정보와 반려동물 id로 정보 삭제
        Place place = placeRepository.findById(requestDto.getId()).orElseThrow(() -> new IllegalArgumentException("해당 id를 가진 장소 마커가 없습니다."));

        placeRepository.delete(place);
    }
}
