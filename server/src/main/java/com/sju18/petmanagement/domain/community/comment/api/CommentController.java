package com.sju18.petmanagement.domain.community.comment.api;

import com.sju18.petmanagement.domain.community.comment.application.CommentService;
import com.sju18.petmanagement.domain.community.comment.dao.Comment;
import com.sju18.petmanagement.domain.community.comment.dto.*;
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
public class CommentController {
    private static final Logger logger = LogManager.getLogger();
    private final MessageSource msgSrc = MessageConfig.getCommunityMessageSource();
    private final CommentService commentServ;

    // CREATE
    @PostMapping("/api/comment/create")
    public ResponseEntity<?> createComment(Authentication auth, @Valid @RequestBody CreateCommentReqDto reqDto) {
        DtoMetadata dtoMetadata;

        try {
            commentServ.createComment(auth, reqDto);
        } catch (Exception e) {
            logger.warn(e.toString());
            dtoMetadata = new DtoMetadata(e.getMessage(), e.getClass().getName());
            return ResponseEntity.status(400).body(new CreateCommentResDto(dtoMetadata));
        }
        dtoMetadata = new DtoMetadata(msgSrc.getMessage("res.comment.create.success", null, Locale.ENGLISH));
        return ResponseEntity.ok(new CreateCommentResDto(dtoMetadata));
    }

    // READ
    @PostMapping("/api/comment/fetch")
    public ResponseEntity<?> fetchComment(@Valid @RequestBody FetchCommentReqDto reqDto) {
        DtoMetadata dtoMetadata;
        final List<Comment> commentList;
        Pageable pageable = null;
        Boolean isLast = null;

        try {
            if (reqDto.getId() != null) {
                // 개별 댓글/댓답글 조회 요청
                commentList = new ArrayList<>();
                commentList.add(commentServ.fetchCommentById(reqDto.getId()));
            } else if (reqDto.getParentCommentId() != null) {
                // 댓답글 목록 조회 요청
                final Page<Comment> commentPage = commentServ.fetchCommentByParentCommentId(reqDto);
                commentList = commentPage.getContent();
                pageable = commentPage.getPageable();
                isLast = commentPage.isLast();
            } else {
                // 댓글 목록 조회 요청
                final Page<Comment> commentPage = commentServ.fetchCommentByPostId(reqDto);
                commentList = commentPage.getContent();
                pageable = commentPage.getPageable();
                isLast = commentPage.isLast();
            }
        } catch (Exception e) {
            logger.warn(e.toString());
            dtoMetadata = new DtoMetadata(e.getMessage(), e.getClass().getName());
            return ResponseEntity.status(400).body(new FetchCommentResDto(dtoMetadata));
        }
        dtoMetadata = new DtoMetadata(msgSrc.getMessage("res.comment.fetch.success", null, Locale.ENGLISH));
        return ResponseEntity.ok(new FetchCommentResDto(dtoMetadata, commentList, pageable, isLast));
    }

    // UPDATE
    @PostMapping("/api/comment/update")
    public ResponseEntity<?> updateComment(Authentication auth, @Valid @RequestBody UpdateCommentReqDto reqDto) {
        DtoMetadata dtoMetadata;
        try {
            commentServ.updateComment(auth, reqDto);
        } catch (Exception e) {
            logger.warn(e.toString());
            dtoMetadata = new DtoMetadata(e.getMessage(), e.getClass().getName());
            return ResponseEntity.status(400).body(new UpdateCommentResDto(dtoMetadata));
        }
        dtoMetadata = new DtoMetadata(msgSrc.getMessage("res.comment.update.success", null, Locale.ENGLISH));
        return ResponseEntity.ok(new UpdateCommentResDto(dtoMetadata));
    }

    // DELETE
    @PostMapping("/api/comment/delete")
    public ResponseEntity<?> deleteComment(Authentication auth, @Valid @RequestBody DeleteCommentReqDto reqDto) {
        DtoMetadata dtoMetadata;
        try {
            commentServ.deleteComment(auth, reqDto);
        } catch (Exception e) {
            logger.warn(e.toString());
            dtoMetadata = new DtoMetadata(e.getMessage(), e.getClass().getName());
            return ResponseEntity.status(400).body(new DeleteCommentResDto(dtoMetadata));
        }
        dtoMetadata = new DtoMetadata(msgSrc.getMessage("res.comment.delete.success", null, Locale.ENGLISH));
        return ResponseEntity.ok(new DeleteCommentResDto(dtoMetadata));
    }
}
