package com.sju18.petmanagement.domain.community.api;

import com.sju18.petmanagement.domain.community.application.PostService;
import com.sju18.petmanagement.domain.community.dao.Post;
import com.sju18.petmanagement.domain.community.dto.*;
import com.sju18.petmanagement.global.common.DtoMetadata;
import com.sju18.petmanagement.global.message.MessageConfig;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
    public ResponseEntity<?> fetchPost(@Valid @RequestBody FetchPostReqDto reqDto) {
        DtoMetadata dtoMetadata;
        final List<Post> postList;
        Pageable pageable = null;
        Boolean isLast = null;

        try {
            if (reqDto.getId() != null) {
                postList = new ArrayList<>();
                postList.add(postServ.fetchPostById(reqDto.getId()));
            } else if (reqDto.getPetId() != null && reqDto.getPageIndex() != null) {
                final Page<Post> postPage = postServ.fetchPostByPet(reqDto.getPageIndex(), reqDto.getPetId());
                postList = postPage.getContent();
                pageable = postPage.getPageable();
                isLast = postPage.isLast();
            } else if (reqDto.getPageIndex() != null) {
                final Page<Post> postPage = postServ.fetchPostByDefault(reqDto.getPageIndex());
                postList = postPage.getContent();
                pageable = postPage.getPageable();
                isLast = postPage.isLast();
            } else {
                throw new Exception(msgSrc.getMessage("error.post.undefinedOperation", null, Locale.ENGLISH));
            }
        } catch (Exception e) {
            logger.warn(e.toString());
            dtoMetadata = new DtoMetadata(e.getMessage(), e.getClass().getName());
            return ResponseEntity.status(400).body(new FetchPostResDto(dtoMetadata));
        }
        dtoMetadata = new DtoMetadata(msgSrc.getMessage("res.post.fetch.success", null, Locale.ENGLISH));
        return ResponseEntity.ok(new FetchPostResDto(dtoMetadata, postList, pageable, isLast));
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
