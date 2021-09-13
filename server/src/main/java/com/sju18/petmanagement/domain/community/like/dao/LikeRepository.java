package com.sju18.petmanagement.domain.community.like.dao;

import com.sju18.petmanagement.domain.account.dao.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
    Long countAllByLikedPostId(Long likedPostId);
    List<Like> findAllByLikedPostId(Long likedPostId);
    Long countAllByLikedCommentId(Long likedCommentId);
    List<Like> findAllByLikedCommentId(Long likedCommentId);
    Optional<Like> findByLikedPostIdAndLikedAccount(Long likedPostId, Account likedAccountId);
    Optional<Like> findByLikedCommentIdAndLikedAccount(Long likedCommentId, Account likedAccountId);
    Boolean existsByLikedPostIdAndLikedAccount(Long likedPostId, Account likedAccountId);
    Boolean existsByLikedCommentIdAndLikedAccount(Long likedCommentId, Account likedAccountId);
}
