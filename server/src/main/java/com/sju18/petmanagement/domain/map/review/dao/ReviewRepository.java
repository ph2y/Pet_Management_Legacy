package com.sju18.petmanagement.domain.map.review.dao;

import com.sju18.petmanagement.domain.account.dao.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    Optional<Review> findByAuthorAndId(Account author, Long id);
    List<Review> findAllByPlaceId(Long placeId);
    List<Review> findAllByAuthor(Account author);
}
