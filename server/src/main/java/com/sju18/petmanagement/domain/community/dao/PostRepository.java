package com.sju18.petmanagement.domain.community.dao;

import com.sju18.petmanagement.domain.account.dao.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    Optional<Post> findById(Long id);
    Optional<Post> findByAuthorAndId(Account author, Long id);
    @Query(
            value = "SELECT * FROM Post AS p WHERE p.pet_id=:petId",
            countQuery = "SELECT COUNT(*) FROM Post AS p WHERE p.pet_id=:petId",
            nativeQuery = true
    )
    Page<Post> findAllByTaggedPetId(@Param("petId") Long taggedPetId, Pageable pageable);
    @Query(
            value = "SELECT * FROM Post AS p WHERE p.disclosure=\"PUBLIC\" OR (p.disclosure=\"FRIEND\" AND p.account_id IN :friends) OR (p.disclosure=\"PRIVATE\" AND p.account_id=:me)",
            countQuery = "SELECT COUNT(*) FROM Post AS p WHERE p.disclosure=\"PUBLIC\" OR (p.disclosure=\"FRIEND\" AND p.account_id IN :friends) OR (p.disclosure=\"PRIVATE\" AND p.account_id=:me)",
            nativeQuery = true
    )
    Page<Post> findMainPosts(
            @Param("friends") Collection<Long> friendAccountIdList,
            @Param("me") Long myAccountId,
            Pageable pageable
    );
}
