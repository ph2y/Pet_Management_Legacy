package com.sju18.petmanagement.domain.community.dao;

import com.sju18.petmanagement.domain.account.dao.Account;
import com.sju18.petmanagement.domain.pet.dao.Pet;
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
    Page<Post> findAllByPet(Pet taggedPet, Pageable pageable);
    Optional<Post> findById(Long id);
    Optional<Post> findByAuthorAndId(Account author, Long id);
    @Query(
            value = "SELECT p FROM Post AS p WHERE p.disclosure=\"PUBLIC\" OR (p.disclosure=\"FRIEND\" AND p.author IN :friends)",
            countQuery = "SELECT COUNT(p) FROM Post AS p WHERE p.disclosure=\"PUBLIC\" OR (p.disclosure=\"FRIEND\" AND p.author IN :friends)"
    )
    Page<Post> findMainPosts(@Param("friends") Collection<Account> friends, Pageable pageable);
}
