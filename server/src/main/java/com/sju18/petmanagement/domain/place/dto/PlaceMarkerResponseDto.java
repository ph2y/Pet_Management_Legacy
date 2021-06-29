package com.sju18.petmanagement.domain.place.dto;

import com.sju18.petmanagement.domain.pet.dao.Pet;
import com.sju18.petmanagement.domain.place.dao.Place;
import lombok.Data;

@Data
public class PlaceMarkerResponseDto {
    private Long id;
    private String name;
    private String memo;
    private Double latitude;
    private Double longitude;

    public PlaceMarkerResponseDto(Place place) {
        this.id = place.getId();
        this.name = place.getName();
        this.memo = place.getMemo();
        this.latitude = place.getLatitude();
        this.longitude = place.getLongitude();
    }
}
