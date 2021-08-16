package com.sju18.petmanagement.domain.map.place.dao;

import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Place {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "place_id")
    private Long id;

    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String categoryName;
    @Column(nullable = false)
    private String categoryCode;
    @Column(nullable = false)
    private String addressName;
    @Column(nullable = false)
    private String roadAddressName;
    @Column(nullable = false)
    private Double latitude;
    @Column(nullable = false)
    private Double longitude;
    @Column(nullable = false)
    private Boolean isOfficial;
    @Column
    private Double averageRating;
    private String phone;
    @Lob
    private String description;
    private String operationHour;
}
