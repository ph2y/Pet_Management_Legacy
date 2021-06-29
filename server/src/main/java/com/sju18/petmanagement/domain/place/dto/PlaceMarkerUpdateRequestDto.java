package com.sju18.petmanagement.domain.place.dto;

import lombok.Data;

@Data
public class PlaceMarkerUpdateRequestDto {
    private Long id;
    private String name;
    private String memo;
    private Double latitude;
    private Double longitude;
}
