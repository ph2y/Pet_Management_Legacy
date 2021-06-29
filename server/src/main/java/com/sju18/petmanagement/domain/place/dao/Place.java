package com.sju18.petmanagement.domain.place.dao;

import com.sju18.petmanagement.domain.place.dto.PlaceMarkerUpdateRequestDto;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Getter
@NoArgsConstructor
@Entity
public class Place {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String username;

    @Column
    private String name;

    @Column
    private String memo;

    @Column
    private Double latitude ;

    @Column
    private Double longitude;

    @Builder
    public Place(String username, String name, String memo, Double latitude, Double longitude) {
        this.username = username;
        this.name = name;
        this.memo = memo;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Long update(PlaceMarkerUpdateRequestDto requestDto) {
        this.name = requestDto.getName();
        this.memo = requestDto.getMemo();
        this.latitude = requestDto.getLatitude();
        this.longitude = requestDto.getLongitude();

        return this.id;
    }
}
