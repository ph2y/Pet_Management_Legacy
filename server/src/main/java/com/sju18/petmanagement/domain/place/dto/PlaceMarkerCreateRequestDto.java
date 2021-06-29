package com.sju18.petmanagement.domain.place.dto;

import com.sju18.petmanagement.domain.place.dao.Place;
import lombok.Builder;
import lombok.Data;

@Data
public class PlaceMarkerCreateRequestDto {
    private String name;
    private String memo;
    private Double latitude;
    private Double longitude;

    @Builder
    public PlaceMarkerCreateRequestDto(String name, String memo, Double latitude, Double longitude) {
        this.name = name;
        this.memo = memo;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Place toEntity(String username) {
        return Place.builder()
                .username(username)
                .name(name)
                .memo(memo)
                .latitude(latitude)
                .longitude(longitude)
                .build();
    }
}
