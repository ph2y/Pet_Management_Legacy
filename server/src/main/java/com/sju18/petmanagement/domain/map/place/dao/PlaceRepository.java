package com.sju18.petmanagement.domain.map.place.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlaceRepository extends JpaRepository<Place, Long> {
    Optional<Place> findById(Long id);
    @Query(
            value = "SELECT * FROM place AS p WHERE (:latMin <= p.latitude AND p.latitude <= :latMax) AND (:longMin <= p.longitude AND p.longitude <= :longMax)",
            nativeQuery = true
    )
    List<Place> findAllByDistance(
            @Param("latMin") Double latMin,
            @Param("latMax") Double latMax,
            @Param("longMin") Double longMin,
            @Param("longMax") Double longMax
    );
}
