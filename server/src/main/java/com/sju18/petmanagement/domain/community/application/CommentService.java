package com.sju18.petmanagement.domain.community.application;

import com.sju18.petmanagement.domain.account.application.AccountService;
import com.sju18.petmanagement.domain.account.dao.Account;
import com.sju18.petmanagement.domain.community.dao.Comment;
import com.sju18.petmanagement.domain.community.dao.CommentRepository;
import com.sju18.petmanagement.domain.community.dao.Post;
import com.sju18.petmanagement.domain.community.dto.CreateCommentReqDto;
import com.sju18.petmanagement.domain.community.dto.DeleteCommentReqDto;
import com.sju18.petmanagement.domain.community.dto.FetchCommentReqDto;
import com.sju18.petmanagement.domain.community.dto.UpdateCommentReqDto;
import com.sju18.petmanagement.global.message.MessageConfig;
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
import java.util.Locale;

@RequiredArgsConstructor
@Service
public class CommentService {
    private final MessageSource msgSrc = MessageConfig.getCommunityMessageSource();
    private final CommentRepository commentRepository;
    private final AccountService accountServ;
    private final PostService postService;

    // CREATE
    @Transactional
    public void createComment(Authentication auth, CreateCommentReqDto reqDto) throws Exception {
        Account author = accountServ.fetchCurrentAccount(auth);
        Post commentedPost = postService.fetchPostById(reqDto.getPostId());
        Comment repliedComment = this.fetchCommentById(reqDto.getParentCommentId());


        // 받은 사용자 정보와 새 입력 정보로 새 댓글 정보 생성
        Comment comment = Comment.builder()
                .author(author)
                .post(commentedPost)
                .postId(commentedPost.getId())
                .parentComment(repliedComment)
                .parentCommentId(repliedComment.getId())
                .contents(reqDto.getContents())
                .timestamp(LocalDateTime.now())
                .edited(false)
                .build();

        // save
        commentRepository.save(comment);
    }

    // READ
    @Transactional(readOnly = true)
    public Page<Comment> fetchCommentByPostId(FetchCommentReqDto reqDto) {
        // 기본 조건에 따른 최신 댓글 인출 (게시물 댓글화면 조회시)
        // 조건: 가장 최신 댓글 50개 조회
        // 추가조건: 만약 fromId(최초 로딩 시점)를 설정했다면 해당 시점 이전의 댓글만 검색
        if (reqDto.getPageIndex() == null) {
            reqDto.setPageIndex(0);
        }
        Pageable pageQuery = PageRequest.of(reqDto.getPageIndex(), 50, Sort.Direction.DESC, "comment_id");

        if (reqDto.getTopCommentId() != null) {
            return commentRepository
                    .findAllByPostIdAndTopCommentId(reqDto.getTopCommentId(), reqDto.getPostId(), pageQuery);
        } else {
            return commentRepository.findAllByPostId(reqDto.getPostId(), pageQuery);
        }
    }

    @Transactional(readOnly = true)
    public Page<Comment> fetchCommentByParentCommentId(FetchCommentReqDto reqDto) {
        // 기본 조건에 따른 최신 댓답글 인출 (댓글 답글화면 조회시)
        // 조건: 가장 최신 댓답글 50개 조회
        // 추가조건: 만약 fromId(최초 로딩 시점)를 설정했다면 해당 시점 이전의 댓답글만 검색
        if (reqDto.getPageIndex() == null) {
            reqDto.setPageIndex(0);
        }
        Pageable pageQuery = PageRequest.of(reqDto.getPageIndex(), 50, Sort.Direction.DESC, "comment_id");

        if (reqDto.getTopCommentId() != null) {
            return commentRepository
                    .findAllByParentCommentIdAndTopCommentId(
                            reqDto.getTopCommentId(), reqDto.getParentCommentId(), pageQuery
                    );
        } else {
            return commentRepository.findAllByParentCommentId(reqDto.getParentCommentId(), pageQuery);
        }
    }

    @Transactional
    public Comment fetchCommentById(Long commentId) throws Exception {
        // 댓글/댓답글 고유번호로 댓글/댓답글 인출 (댓글/댓답글 단일 불러오기시 사용)
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new Exception(
                        msgSrc.getMessage("error.comment.notExists", null, Locale.ENGLISH)
                ));
    }

    // UPDATE
    public void updateComment(Authentication auth, UpdateCommentReqDto reqDto) throws Exception {
        // 받은 사용자 정보와 댓글/댓답글 id로 댓글/댓답글 정보 수정
        Account author = accountServ.fetchCurrentAccount(auth);
        Comment currentComment = commentRepository.findByAuthorAndId(author, reqDto.getId())
                .orElseThrow(() -> new Exception(
                        msgSrc.getMessage("error.comment.notExists", null, Locale.ENGLISH)
                ));

        if(reqDto.getContents() != null && !reqDto.getContents().equals(currentComment.getContents())) {
            currentComment.setContents(reqDto.getContents());
        }
        currentComment.setEdited(true);

        // save
        commentRepository.save(currentComment);
    }

    // DELETE
    public void deleteComment(Authentication auth, DeleteCommentReqDto reqDto) throws Exception {
        // 받은 사용자 정보와 댓글/댓답글 id로 댓글/댓답글 정보 삭제
        Account author = accountServ.fetchCurrentAccount(auth);
        Comment comment = commentRepository.findByAuthorAndId(author, reqDto.getId())
                .orElseThrow(() -> new Exception(
                        msgSrc.getMessage("error.comment.notExists", null, Locale.ENGLISH)
                ));
        commentRepository.save(comment);
    }
}
