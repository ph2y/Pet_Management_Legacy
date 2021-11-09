package com.sju18.petmanagement.domain.map.place.dto;

import lombok.Data;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

@Data
public class FetchPlaceReqDto {
    @PositiveOrZero(message = "valid.place.id.notNegative")
    private Long id;
    @DecimalMax(value = "90.0", message = "valid.place.latitude.max")
    @DecimalMin(value = "-90.0", message = "valid.place.latitude.min")
    private BigDecimal currentLat;
    @DecimalMax(value = "180.0", message = "valid.place.longitude.max")
    @DecimalMin(value = "-180.0", message = "valid.place.longitude.min")
    private BigDecimal currentLong;
    @DecimalMax(value = "5000.0", message = "valid.place.range.max")
    @DecimalMin(value = "0.0", message = "valid.place.range.min")
    private BigDecimal range;
}
