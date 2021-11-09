package com.sju18.petmanagement.domain.map.place.dto;

import com.sju18.petmanagement.domain.map.place.dao.Place;
import com.sju18.petmanagement.global.common.DtoMetadata;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class FetchPlaceResDto {
    private DtoMetadata _metadata;
    private List<Place> placeList;
}
