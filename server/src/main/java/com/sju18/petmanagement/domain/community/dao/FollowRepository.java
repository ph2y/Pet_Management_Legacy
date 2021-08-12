package com.sju18.petmanagement.domain.community.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {
    List<Follow> findAllByFollowingId(Long following_id);
    List<Follow> findAllByFollowerId(Long follower_id);
    Optional<Follow> findByFollowerIdAndFollowingId(Long follower_id, Long following_id);
}
