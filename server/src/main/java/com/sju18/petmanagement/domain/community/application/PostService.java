package com.sju18.petmanagement.domain.community.application;

import com.sju18.petmanagement.domain.account.application.AccountService;
import com.sju18.petmanagement.domain.account.dao.Account;
import com.sju18.petmanagement.domain.community.dao.Post;
import com.sju18.petmanagement.domain.community.dao.PostRepository;
import com.sju18.petmanagement.domain.community.dto.CreatePostReqDto;
import com.sju18.petmanagement.domain.community.dto.DeletePostReqDto;
import com.sju18.petmanagement.domain.community.dto.UpdatePostReqDto;
import com.sju18.petmanagement.domain.pet.application.PetService;
import com.sju18.petmanagement.domain.pet.dao.Pet;
import com.sju18.petmanagement.global.message.MessageConfig;
import com.sju18.petmanagement.global.storage.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Locale;

@RequiredArgsConstructor
@Service
public class PostService {
    private final MessageSource msgSrc = MessageConfig.getCommunityMessageSource();
    private final PostRepository postRepository;
    private final AccountService accountServ;
    private final PetService petServ;
    private final FileService fileServ;

    // CREATE
    @Transactional
    public void createPost(Authentication auth, CreatePostReqDto reqDto) throws Exception {
        Account author = accountServ.fetchCurrentAccount(auth);
        Pet taggedPet = petServ.fetchPetById(reqDto.getPetId());

        // 받은 사용자 정보와 새 입력 정보로 새 게시물 정보 생성
        Post post = Post.builder()
                .author(author)
                .pet(taggedPet)
                .contents(reqDto.getContents())
                .timestamp(LocalDateTime.now())
                .edited(false)
                .serializedHashTags(String.join(",", reqDto.getHashTags()))
                .disclosure(reqDto.getDisclosure())
                .geoTagLat(reqDto.getGeoTagLat().doubleValue())
                .geoTagLong(reqDto.getGeoTagLong().doubleValue())
                .build();
        
        // save
        postRepository.save(post);
        
        // 게시물 파일 저장소 생성
        fileServ.createPostFileStorage(post.getId());
    }

    // READ
    @Transactional(readOnly = true)
    public Page<Post> fetchPostByDefault(int pageIndex) {
        // 기본 조건에 따른 최신 게시물 인출 (커뮤니티 메인화면 조회시)
        // 조건: 가장 최신의 전체 공개 게시물 또는 친구의 게시물 10개 조회
        Pageable pageQuery = PageRequest.of(pageIndex, 10, Sort.Direction.DESC, "id");

        return postRepository.findMainPosts(Collections.emptyList(), pageQuery);
    }

    @Transactional(readOnly = true)
    public Page<Post> fetchPostByPet(int pageIndex, Long petId) {
        // 태그된 펫으로 게시물 인출 (펫 피드 조회시)
        Pageable pageQuery = PageRequest.of(pageIndex,10, Sort.Direction.DESC, "id");
        Pet taggedPet = petServ.fetchPetById(petId);

        return postRepository.findAllByPet(taggedPet, pageQuery);
    }

    @Transactional(readOnly = true)
    public Post fetchPostById(Long postId) throws Exception {
        // 게시물 고유번호로 게시물 인출 (게시물 단일 불러오기시 사용)
        return postRepository.findById(postId)
                .orElseThrow(() -> new Exception(
                        msgSrc.getMessage("error.post.notExists", null, Locale.ENGLISH)
                ));
    }

    // UPDATE
    @Transactional
    public void updatePost(Authentication auth, UpdatePostReqDto reqDto) throws Exception {
        // 받은 사용자 정보와 게시물 id로 게시물 정보 수정
        Account author = accountServ.fetchCurrentAccount(auth);
        Post currentPost = postRepository.findByAuthorAndId(author, reqDto.getId())
                .orElseThrow(() -> new Exception(
                        msgSrc.getMessage("error.post.notExists", null, Locale.ENGLISH)
                ));

        if (!reqDto.getPetId().equals(currentPost.getPet().getId())) {
            Pet taggedPet = petServ.fetchPetById(reqDto.getPetId());
            currentPost.setPet(taggedPet);
        }
        if (!reqDto.getContents().equals(currentPost.getContents())) {
            currentPost.setContents(reqDto.getContents());
        }
        if (!String.join(",", reqDto.getHashTags()).equals(currentPost.getSerializedHashTags())) {
            currentPost.setSerializedHashTags(String.join(",", reqDto.getHashTags()));
        }
        if (!reqDto.getDisclosure().equals(currentPost.getDisclosure())) {
            currentPost.setDisclosure(reqDto.getDisclosure());
        }
        if (reqDto.getGeoTagLat().doubleValue() != currentPost.getGeoTagLat()) {
            currentPost.setGeoTagLat(reqDto.getGeoTagLat().doubleValue());
        }
        if (reqDto.getGeoTagLong().doubleValue() != currentPost.getGeoTagLong()) {
            currentPost.setGeoTagLong(reqDto.getGeoTagLong().doubleValue());
        }
        currentPost.setEdited(true);

        // save
        postRepository.save(currentPost);
    }

    // DELETE
    @Transactional
    public void deletePost(Authentication auth, DeletePostReqDto reqDto) throws Exception {
        // 받은 사용자 정보와 게시물 id로 게시물 정보 삭제
        Account author = accountServ.fetchCurrentAccount(auth);
        Post post = postRepository.findByAuthorAndId(author, reqDto.getId())
                .orElseThrow(() -> new Exception(
                        msgSrc.getMessage("error.post.notExists", null, Locale.ENGLISH)
                ));
        fileServ.deletePostFileStorage(post.getId());
        postRepository.delete(post);
    }
}
