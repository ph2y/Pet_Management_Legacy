package com.sju18.petmanagement.domain.community.post.api;

import com.sju18.petmanagement.domain.community.post.application.PostService;
import com.sju18.petmanagement.domain.community.post.dao.Post;
import com.sju18.petmanagement.domain.community.post.dto.*;
import com.sju18.petmanagement.global.common.DtoMetadata;
import com.sju18.petmanagement.global.message.MessageConfig;
import com.sju18.petmanagement.global.storage.FileMetadata;
import com.sju18.petmanagement.global.storage.FileType;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.MessageSource;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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
        Long postId;

        try {
            postId = postServ.createPost(auth, reqDto);
        } catch (Exception e) {
            logger.warn(e.toString());
            dtoMetadata = new DtoMetadata(e.getMessage(), e.getClass().getName());
            return ResponseEntity.status(400).body(new CreatePostResDto(dtoMetadata));
        }
        dtoMetadata = new DtoMetadata(msgSrc.getMessage("res.post.create.success", null, Locale.ENGLISH));
        return ResponseEntity.ok(new CreatePostResDto(dtoMetadata, postId));
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

    @PostMapping("/api/post/image/fetch")
    public ResponseEntity<?> fetchPostImage(@Valid @RequestBody FetchPostImageReqDto reqDto) {
        DtoMetadata dtoMetadata;
        byte[] fileBinData;
        try {
            fileBinData = postServ.fetchPostImage(reqDto.getId(), reqDto.getIndex());
        } catch (Exception e) {
            logger.warn(e.toString());
            dtoMetadata = new DtoMetadata(e.getMessage(), e.getClass().getName());
            return ResponseEntity.status(400).body(new FetchPostImageResDto(dtoMetadata));
        }
        return ResponseEntity.ok(fileBinData);
    }

    @PostMapping("/api/post/file/fetch")
    public ResponseEntity<?> fetchPostFile(@Valid @RequestBody FetchPostFileReqDto reqDto) {
        DtoMetadata dtoMetadata;
        byte[] fileBinData;
        try {
            fileBinData = postServ.fetchPostFile(reqDto.getId(), reqDto.getIndex());
        } catch (Exception e) {
            logger.warn(e.toString());
            dtoMetadata = new DtoMetadata(e.getMessage(), e.getClass().getName());
            return ResponseEntity.status(400).body(new FetchPostFileResDto(dtoMetadata));
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

    @PostMapping("/api/post/file/update")
    public ResponseEntity<?> updatePostFile(Authentication auth, @ModelAttribute UpdatePostFileReqDto reqDto) {
        DtoMetadata dtoMetadata;
        List<FileMetadata> fileMetadataList;
        try {
            fileMetadataList = postServ.updatePostFile(auth, reqDto, FileType.valueOf(reqDto.getFileType().replace("\"", "")));
        } catch (Exception e) {
            logger.warn(e.toString());
            dtoMetadata = new DtoMetadata(e.getMessage(), e.getClass().getName());
            return ResponseEntity.status(400).body(new UpdatePostFileResDto(dtoMetadata, null));
        }
        dtoMetadata = new DtoMetadata(msgSrc.getMessage("res.postFile.update.success", null, Locale.ENGLISH));
        return ResponseEntity.ok(new UpdatePostFileResDto(dtoMetadata, fileMetadataList));
    }

    @PostMapping("/api/post/media/update")
    @Deprecated
    public ResponseEntity<?> updatePostMedia(Authentication auth, @ModelAttribute UpdatePostFileReqDto reqDto) {
        DtoMetadata dtoMetadata;
        List<FileMetadata> fileMetadataList;
        try {
            fileMetadataList = postServ.updatePostFile(auth, reqDto, FileType.IMAGE_FILE);
        } catch (Exception e) {
            logger.warn(e.toString());
            dtoMetadata = new DtoMetadata(e.getMessage(), e.getClass().getName());
            return ResponseEntity.status(400).body(new UpdatePostFileResDto(dtoMetadata, null));
        }
        dtoMetadata = new DtoMetadata(msgSrc.getMessage("res.postMedia.update.success", null, Locale.ENGLISH));
        return ResponseEntity.ok(new UpdatePostFileResDto(dtoMetadata, fileMetadataList));
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

    @PostMapping("/api/post/file/delete")
    public ResponseEntity<?> deletePostFile(Authentication auth, @Valid @RequestBody DeletePostFileReqDto reqDto) {
        DtoMetadata dtoMetadata;
        try {
            postServ.deletePostFile(auth, reqDto, FileType.valueOf(reqDto.getFileType().replace("\"", "")));
        } catch (Exception e) {
            logger.warn(e.toString());
            dtoMetadata = new DtoMetadata(e.getMessage(), e.getClass().getName());
            return ResponseEntity.status(400).body(new DeletePostFileResDto(dtoMetadata));
        }
        dtoMetadata = new DtoMetadata(msgSrc.getMessage("res.postFile.delete.success", null, Locale.ENGLISH));
        return ResponseEntity.ok(new DeletePostFileResDto(dtoMetadata));
    }

    @PostMapping("/api/post/media/delete")
    @Deprecated
    public ResponseEntity<?> deletePostMedia(Authentication auth, @Valid @RequestBody DeletePostFileReqDto reqDto) {
        DtoMetadata dtoMetadata;
        try {
            postServ.deletePostFile(auth, reqDto, FileType.IMAGE_FILE);
        } catch (Exception e) {
            logger.warn(e.toString());
            dtoMetadata = new DtoMetadata(e.getMessage(), e.getClass().getName());
            return ResponseEntity.status(400).body(new DeletePostFileResDto(dtoMetadata));
        }
        dtoMetadata = new DtoMetadata(msgSrc.getMessage("res.postFile.delete.success", null, Locale.ENGLISH));
        return ResponseEntity.ok(new DeletePostFileResDto(dtoMetadata));
    }
}
