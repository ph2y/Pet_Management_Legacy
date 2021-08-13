package com.sju18.petmanagement.domain.community.application;

import com.sju18.petmanagement.domain.account.application.AccountService;
import com.sju18.petmanagement.domain.account.dao.Account;
import com.sju18.petmanagement.domain.community.dao.Comment;
import com.sju18.petmanagement.domain.community.dao.Like;
import com.sju18.petmanagement.domain.community.dao.LikeRepository;
import com.sju18.petmanagement.domain.community.dao.Post;
import com.sju18.petmanagement.domain.community.dto.CreateLikeReqDto;
import com.sju18.petmanagement.domain.community.dto.DeleteLikeReqDto;
import com.sju18.petmanagement.domain.community.dto.FetchLikeReqDto;
import com.sju18.petmanagement.global.exception.DtoValidityException;
import com.sju18.petmanagement.global.message.MessageConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@RequiredArgsConstructor
@Service
public class LikeService {
    private final MessageSource msgSrc = MessageConfig.getCommunityMessageSource();
    private final LikeRepository likeRepository;
    private final AccountService accountServ;
    private final PostService postServ;
    private final CommentService commentServ;

    // CREATE
    @Transactional
    public void createLike(Authentication auth, CreateLikeReqDto reqDto) throws Exception {
        // 받은 사용자 정보와 게시물/댓글/댓답글 id로 새 좋아요 정보 생성
        Account likedAccount = accountServ.fetchCurrentAccount(auth);
        Post likedPost = null;
        Comment likedComment = null;
        if (reqDto.getCommentId() != null) {
            likedComment = commentServ.fetchCommentById(reqDto.getCommentId());
            this.checkAlreadyLikedComment(likedComment.getId(), likedAccount);
        } else {
            likedPost = postServ.fetchPostById(reqDto.getPostId());
            this.checkAlreadyLikedPost(likedPost.getId(), likedAccount);
        }

        // 이미 해당 게시물/댓글/댓답글을 좋아요하였는지 검증


        Like like = Like.builder()
                .likedAccount(likedAccount)
                .likedAccountId(likedAccount.getId())
                .likedPost(likedPost)
                .likedPostId(likedPost != null ? likedPost.getId() : null)
                .likedComment(likedComment)
                .likedCommentId(likedComment != null ? likedComment.getId() : null)
                .build();

        // save
        likeRepository.save(like);
    }

    private void checkAlreadyLikedPost(Long postId, Account likedAccount) throws Exception {
        if (likeRepository.existsByLikedPostIdAndLikedAccount(postId, likedAccount)) {
            throw new DtoValidityException(
                    msgSrc.getMessage("error.dup.like",null, Locale.ENGLISH)
            );
        }
    }

    private void checkAlreadyLikedComment(Long commentId, Account likedAccount) throws Exception {
        if (likeRepository.existsByLikedCommentIdAndLikedAccount(commentId, likedAccount)) {
            throw new DtoValidityException(
                    msgSrc.getMessage("error.dup.like",null, Locale.ENGLISH)
            );
        }
    }

    // READ
    @Transactional
    public Long fetchLike(FetchLikeReqDto reqDto) {
        // 게시물/댓글/댓답글 id로 좋아요 갯수 인출
        if (reqDto.getCommentId() != null) {
            return likeRepository.countAllByLikedCommentId(reqDto.getCommentId());
        } else {
            return likeRepository.countAllByLikedPostId(reqDto.getPostId());
        }
    }

    // DELETE
    @Transactional
    public void deleteLike(Authentication auth, DeleteLikeReqDto reqDto) throws Exception {
        // 받은 사용자 정보와 게시물/댓글/댓답글 id로 좋아요 취소
        Like like;
        Account likedAccount = accountServ.fetchCurrentAccount(auth);
        if (reqDto.getCommentId() != null) {
            like = likeRepository.findByLikedCommentIdAndLikedAccount(reqDto.getCommentId(), likedAccount)
                    .orElseThrow(() -> new Exception(
                            msgSrc.getMessage("error.like.notExists", null, Locale.ENGLISH)
                    ));
        } else {
            like = likeRepository.findByLikedPostIdAndLikedAccount(reqDto.getPostId(), likedAccount)
                    .orElseThrow(() -> new Exception(
                            msgSrc.getMessage("error.like.notExists", null, Locale.ENGLISH)
                    ));
        }
        likeRepository.delete(like);
    }
}
