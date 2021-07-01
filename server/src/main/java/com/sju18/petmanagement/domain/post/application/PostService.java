package com.sju18.petmanagement.domain.post.application;


import com.sju18.petmanagement.domain.post.dao.Post;
import com.sju18.petmanagement.domain.post.dao.PostRepository;
import com.sju18.petmanagement.domain.post.dto.PostCreateRequestDto;
import com.sju18.petmanagement.domain.post.dto.PostDeleteRequestDto;
import com.sju18.petmanagement.domain.post.dto.PostResponseDto;
import com.sju18.petmanagement.domain.post.dto.PostUpdateRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class PostService {
    private final PostRepository postRepository;

    String getUserNameFromToken(Authentication authentication) {
        // 현재 사용자 정보 조회
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return userDetails.getUsername();
    }

    // CREATE
    @Transactional
    public Long createPlaceMarker(Authentication authentication, PostCreateRequestDto requestDto) {
        String username = getUserNameFromToken(authentication);

        // 받은 사용자 정보와 새 입력 정보로 새 포스트 생성
        try {
            return postRepository.save(requestDto.toEntity(username)).getId();
        } catch (Exception e) {
            return (long) -1;
        }
    }

    // READ
    @Transactional(readOnly = true)
    public List<PostResponseDto> fetchPlaceMarker(Authentication authentication) {
        String username = getUserNameFromToken(authentication);

        // 사용자 정보로 포스트 리스트 인출
        return postRepository.findAllByUsername(username).stream()
                .map(PostResponseDto::new)
                .collect(Collectors.toList());
    }

    // UPDATE
    @Transactional
    public Long updatePlaceMarker(Authentication authentication, PostUpdateRequestDto requestDto) {
        String username = getUserNameFromToken(authentication);

        // 받은 사용자 정보와 입력 정보로 포스트 업데이트
        Post post = postRepository.findById(requestDto.getId()).orElseThrow(() -> new IllegalArgumentException("해당 id를 가진 포스트가 없습니다."));

        return post.update(requestDto);
    }

    // DELETE
    @Transactional
    public void deletePlaceMarker(Authentication authentication, PostDeleteRequestDto requestDto) {
        String username = getUserNameFromToken(authentication);

        // 받은 사용자 정보와 포스트 id로 포스트 삭제
        Post post = postRepository.findById(requestDto.getId()).orElseThrow(() -> new IllegalArgumentException("해당 id를 가진 포스트가 없습니다."));

        postRepository.delete(post);
    }
}
