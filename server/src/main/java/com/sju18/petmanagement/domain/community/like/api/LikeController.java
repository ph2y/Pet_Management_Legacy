package com.sju18.petmanagement.domain.community.like.api;

import com.sju18.petmanagement.domain.community.like.application.LikeService;
import com.sju18.petmanagement.domain.community.like.dto.*;
import com.sju18.petmanagement.global.common.DtoMetadata;
import com.sju18.petmanagement.global.message.MessageConfig;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.Locale;

@RequiredArgsConstructor
@RestController
public class LikeController {
    private static final Logger logger = LogManager.getLogger();
    private final MessageSource msgSrc = MessageConfig.getCommunityMessageSource();
    private final LikeService likeServ;

    // CREATE
    @PostMapping("/api/like/create")
    public ResponseEntity<?> createLike(Authentication auth, @Valid @RequestBody CreateLikeReqDto reqDto) {
        DtoMetadata dtoMetadata;

        try {
            likeServ.createLike(auth, reqDto);
        } catch (Exception e) {
            logger.warn(e.toString());
            dtoMetadata = new DtoMetadata(e.getMessage(), e.getClass().getName());
            return ResponseEntity.status(400).body(new CreateLikeResDto(dtoMetadata));
        }
        dtoMetadata = new DtoMetadata(msgSrc.getMessage("res.like.create.success", null, Locale.ENGLISH));
        return ResponseEntity.ok(new CreateLikeResDto(dtoMetadata));
    }

    // READ
    @PostMapping("/api/like/fetch")
    public ResponseEntity<?> fetchLike(@Valid @RequestBody FetchLikeReqDto reqDto) {
        DtoMetadata dtoMetadata;
        final Long likeCount;
        final List<Long> likedAccountIdList;
        try {
            likeCount = likeServ.fetchLikeCount(reqDto);
            likedAccountIdList = likeServ.fetchLikeAccountIdList(reqDto);
        } catch (Exception e) {
            logger.warn(e.toString());
            dtoMetadata = new DtoMetadata(e.getMessage(), e.getClass().getName());
            return ResponseEntity.status(400).body(new FetchLikeResDto(dtoMetadata, null, null));
        }
        dtoMetadata = new DtoMetadata(msgSrc.getMessage("res.like.fetch.success", null, Locale.ENGLISH));
        return ResponseEntity.ok(new FetchLikeResDto(dtoMetadata, likeCount, likedAccountIdList));
    }

    // DELETE
    @PostMapping("/api/like/delete")
    public ResponseEntity<?> deleteLike(Authentication auth, @Valid @RequestBody DeleteLikeReqDto reqDto) {
        DtoMetadata dtoMetadata;

        try {
            likeServ.deleteLike(auth, reqDto);
        } catch (Exception e) {
            logger.warn(e.toString());
            dtoMetadata = new DtoMetadata(e.getMessage(), e.getClass().getName());
            return ResponseEntity.status(400).body(new DeleteLikeResDto(dtoMetadata));
        }
        dtoMetadata = new DtoMetadata(msgSrc.getMessage("res.like.delete.success", null, Locale.ENGLISH));
        return ResponseEntity.ok(new DeleteLikeResDto(dtoMetadata));
    }
}
