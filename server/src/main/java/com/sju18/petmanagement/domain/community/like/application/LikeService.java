package com.sju18.petmanagement.domain.community.like.application;

import com.sju18.petmanagement.domain.account.application.AccountService;
import com.sju18.petmanagement.domain.account.dao.Account;
import com.sju18.petmanagement.domain.community.comment.application.CommentService;
import com.sju18.petmanagement.domain.community.post.application.PostService;
import com.sju18.petmanagement.domain.community.comment.dao.Comment;
import com.sju18.petmanagement.domain.community.like.dao.Like;
import com.sju18.petmanagement.domain.community.like.dao.LikeRepository;
import com.sju18.petmanagement.domain.community.post.dao.Post;
import com.sju18.petmanagement.domain.community.like.dto.CreateLikeReqDto;
import com.sju18.petmanagement.domain.community.like.dto.DeleteLikeReqDto;
import com.sju18.petmanagement.domain.community.like.dto.FetchLikeReqDto;
import com.sju18.petmanagement.global.exception.DtoValidityException;
import com.sju18.petmanagement.global.message.MessageConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

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
    public Long fetchLikeCount(FetchLikeReqDto reqDto) {
        // 게시물/댓글/댓답글 id로 좋아요 갯수 인출
        if (reqDto.getCommentId() != null) {
            return likeRepository.countAllByLikedCommentId(reqDto.getCommentId());
        } else {
            return likeRepository.countAllByLikedPostId(reqDto.getPostId());
        }
    }

    @Transactional
    public List<Long> fetchLikeAccountIdList(FetchLikeReqDto reqDto) {
        // 게시물/댓글/댓답글 id로 좋아요를 누른 Account Id 리스트 인출
        if (reqDto.getCommentId() != null) {
            return likeRepository.findAllByLikedCommentId(reqDto.getCommentId()).stream().map(Like::getLikedAccountId).collect(Collectors.toList());
        } else {
            return likeRepository.findAllByLikedPostId(reqDto.getPostId()).stream().map(Like::getLikedAccountId).collect(Collectors.toList());
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
