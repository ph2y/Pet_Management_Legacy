package com.sju18.petmanagement.domain.post.api;


import com.sju18.petmanagement.domain.post.application.PostService;
import com.sju18.petmanagement.domain.post.dto.PostCreateRequestDto;
import com.sju18.petmanagement.domain.post.dto.PostDeleteRequestDto;
import com.sju18.petmanagement.domain.post.dto.PostUpdateRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class PostController {
    private final PostService postService;

    // CREATE
    @PostMapping("/api/post/create")
    public Long createPetInfo(Authentication authentication, @RequestBody PostCreateRequestDto requestDto) {
        return postService.createPlaceMarker(authentication, requestDto);
    }

    // READ
    @PostMapping("/api/post/fetch")
    public ResponseEntity<?> fetchPetInfo(Authentication authentication) {
        return ResponseEntity.ok(postService.fetchPlaceMarker(authentication));
    }

    // UPDATE
    @PostMapping("/api/post/update")
    public Long updatePetInfo(Authentication authentication, @RequestBody PostUpdateRequestDto requestDto) {
        return postService.updatePlaceMarker(authentication, requestDto);
    }

    // DELETE
    @PostMapping("/api/post/delete")
    public void deletePetInfo(Authentication authentication, @RequestBody PostDeleteRequestDto requestDto) {
        postService.deletePlaceMarker(authentication, requestDto);
    }
}
