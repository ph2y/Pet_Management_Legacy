package com.sju18.petmanagement.domain.community.post.api;

import com.sju18.petmanagement.domain.community.post.application.PostService;
import com.sju18.petmanagement.domain.community.post.dao.Post;
import com.sju18.petmanagement.domain.community.post.dto.*;
import com.sju18.petmanagement.global.common.DtoMetadata;
import com.sju18.petmanagement.global.message.MessageConfig;
import com.sju18.petmanagement.global.storage.FileMetadata;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@RequiredArgsConstructor
@RestController
public class PostController {
    private static final Logger logger = LogManager.getLogger();
    private final MessageSource msgSrc = MessageConfig.getCommunityMessageSource();
    private final PostService postServ;

    // CREATE
    @PostMapping("/api/post/create")
    public ResponseEntity<?> createPost(Authentication auth, @Valid @RequestBody CreatePostReqDto reqDto) {
        DtoMetadata dtoMetadata;

        try {
            postServ.createPost(auth, reqDto);
        } catch (Exception e) {
            logger.warn(e.toString());
            dtoMetadata = new DtoMetadata(e.getMessage(), e.getClass().getName());
            return ResponseEntity.status(400).body(new CreatePostResDto(dtoMetadata));
        }
        dtoMetadata = new DtoMetadata(msgSrc.getMessage("res.post.create.success", null, Locale.ENGLISH));
        return ResponseEntity.ok(new CreatePostResDto(dtoMetadata));
    }

    //READ
    @PostMapping("/api/post/fetch")
    public ResponseEntity<?> fetchPost(Authentication auth, @Valid @RequestBody FetchPostReqDto reqDto) {
        DtoMetadata dtoMetadata;
        final List<Post> postList;
        Pageable pageable = null;
        Boolean isLast = null;

        try {
            if (reqDto.getId() != null) {
                // 개별 게시물 조회 요청
                postList = new ArrayList<>();
                postList.add(postServ.fetchPostById(reqDto.getId()));
            } else if (reqDto.getPetId() != null) {
                // 펫 피드 조회 요청
                final Page<Post> postPage = postServ.fetchPostByPet(reqDto.getPetId(), reqDto.getPageIndex());
                postList = postPage.getContent();
                pageable = postPage.getPageable();
                isLast = postPage.isLast();
            } else {
                // 전체 게시물 조회 요청
                final Page<Post> postPage = postServ.fetchPostByDefault(auth, reqDto.getPageIndex(), reqDto.getTopPostId());
                postList = postPage.getContent();
                pageable = postPage.getPageable();
                isLast = postPage.isLast();
            }
        } catch (Exception e) {
            logger.warn(e.toString());
            dtoMetadata = new DtoMetadata(e.getMessage(), e.getClass().getName());
            return ResponseEntity.status(400).body(new FetchPostResDto(dtoMetadata));
        }
        dtoMetadata = new DtoMetadata(msgSrc.getMessage("res.post.fetch.success", null, Locale.ENGLISH));
        return ResponseEntity.ok(new FetchPostResDto(dtoMetadata, postList, pageable, isLast));
    }

    @PostMapping("/api/post/media/fetch")
    public ResponseEntity<?> fetchPostMedia(@Valid @RequestBody FetchPostMediaReqDto reqDto) {
        DtoMetadata dtoMetadata;
        byte[] fileBinData;
        try {
            fileBinData = postServ.fetchPostMedia(reqDto.getId(), reqDto.getIndex());
        } catch (Exception e) {
            logger.warn(e.toString());
            dtoMetadata = new DtoMetadata(e.getMessage(), e.getClass().getName());
            return ResponseEntity.status(400).body(new FetchPostMediaResDto(dtoMetadata));
        }
        return ResponseEntity.ok(fileBinData);
    }

    // UPDATE
    @PostMapping("/api/post/update")
    public ResponseEntity<?> updatePost(Authentication auth, @Valid @RequestBody UpdatePostReqDto reqDto) {
        DtoMetadata dtoMetadata;
        try {
            postServ.updatePost(auth, reqDto);
        } catch (Exception e) {
            logger.warn(e.toString());
            dtoMetadata = new DtoMetadata(e.getMessage(), e.getClass().getName());
            return ResponseEntity.status(400).body(new UpdatePostResDto(dtoMetadata));
        }
        dtoMetadata = new DtoMetadata(msgSrc.getMessage("res.post.update.success", null, Locale.ENGLISH));
        return ResponseEntity.ok(new UpdatePostResDto(dtoMetadata));
    }

    @PostMapping("/api/post/media/update")
    public ResponseEntity<?> updatePostMedia(Authentication auth, @ModelAttribute UpdatePostMediaReqDto reqDto) {
        DtoMetadata dtoMetadata;
        List<FileMetadata> fileMetadataList;
        try {
            fileMetadataList = postServ.updatePostMedia(auth, reqDto);
        } catch (Exception e) {
            logger.warn(e.toString());
            dtoMetadata = new DtoMetadata(e.getMessage(), e.getClass().getName());
            return ResponseEntity.status(400).body(new UpdatePostMediaResDto(dtoMetadata, null));
        }
        dtoMetadata = new DtoMetadata(msgSrc.getMessage("res.postMedia.update.success", null, Locale.ENGLISH));
        return ResponseEntity.ok(new UpdatePostMediaResDto(dtoMetadata, fileMetadataList));
    }

    // DELETE
    @PostMapping("/api/post/delete")
    public ResponseEntity<?> deletePost(Authentication auth, @Valid @RequestBody DeletePostReqDto reqDto) {
        DtoMetadata dtoMetadata;
        try {
            postServ.deletePost(auth, reqDto);
        } catch (Exception e) {
            logger.warn(e.toString());
            dtoMetadata = new DtoMetadata(e.getMessage(), e.getClass().getName());
            return ResponseEntity.status(400).body(new DeletePostResDto(dtoMetadata));
        }
        dtoMetadata = new DtoMetadata(msgSrc.getMessage("res.post.delete.success", null, Locale.ENGLISH));
        return ResponseEntity.ok(new DeletePostResDto(dtoMetadata));
    }
}
