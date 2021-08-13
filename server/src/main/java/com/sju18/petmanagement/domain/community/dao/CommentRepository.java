package com.sju18.petmanagement.domain.community.dao;

import com.sju18.petmanagement.domain.account.dao.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    Optional<Comment> findById(Long id);
    Optional<Comment> findByAuthorAndId(Account author, Long id);

    @Query(
            value = "SELECT * FROM comment AS c WHERE c.post_id = :postId",
            countQuery = "SELECT COUNT(*) FROM comment AS c WHERE c.post_id = :postId",
            nativeQuery = true
    )
    Page<Comment> findAllByPostId(@Param("postId") Long postId, Pageable pageable);

    @Query(
            value = "SELECT * FROM comment AS c WHERE c.comment_id <= :top AND c.post_id = :postId",
            countQuery = "SELECT COUNT(*) FROM comment AS c WHERE c.comment_id <= :top AND c.post_id = :postId",
            nativeQuery = true
    )
    Page<Comment> findAllByPostIdAndTopCommentId(
            @Param("top") Long topCommentId,
            @Param("postId") Long postId,
            Pageable pageable
    );

    @Query(
            value = "SELECT * FROM comment AS c WHERE c.parent_comment_id = :parentId",
            countQuery = "SELECT COUNT(*) FROM comment AS c WHERE c.parent_comment_id = :parentId",
            nativeQuery = true
    )
    Page<Comment> findAllByParentCommentId(@Param("parentId") Long parentCommentId, Pageable pageable);

    @Query(
            value = "SELECT * FROM comment AS c WHERE c.comment_id <= :top AND c.parent_comment_id = :parentId",
            countQuery = "SELECT COUNT(*) FROM comment AS c WHERE c.comment_id <= :top AND c.parent_comment_id = :parentId",
            nativeQuery = true
    )
    Page<Comment> findAllByParentCommentIdAndTopCommentId(
            @Param("top") Long topCommentId,
            @Param("parentId") Long parentCommentId,
            Pageable pageable
    );
}
