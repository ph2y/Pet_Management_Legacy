package com.sju18.petmanagement.domain.map.place.dto;

import lombok.Data;

import javax.validation.constraints.*;
import java.math.BigDecimal;

@Data
public class UpdatePlaceReqDto {
    @PositiveOrZero(message = "valid.place.id.notNegative")
    Long id;
    @Size(max = 20, message = "valid.place.name.size")
    private String name;
    @Size(max = 20, message = "valid.place.categoryName.size")
    private String categoryName;
    @Size(max = 5, message = "valid.place.categoryCode.size")
    private String categoryCode;
    @Size(max = 250, message = "valid.place.addressName.size")
    private String addressName;
    @Size(max = 250, message = "valid.place.roadAddressName.size")
    private String roadAddressName;
    @DecimalMax(value = "90.0", message = "valid.place.latitude.max")
    @DecimalMin(value = "-90.0", message = "valid.place.latitude.min")
    private BigDecimal latitude;
    @DecimalMax(value = "180.0", message = "valid.place.longitude.max")
    @DecimalMin(value = "-180.0", message = "valid.place.longitude.min")
    private BigDecimal longitude;
    private Boolean isOfficial;
    @Pattern(regexp = "(^02|^\\d{3})-(\\d{3}|\\d{4})-\\d{4}", message = "valid.place.phone.phone")
    private String phone;
    @Size(max = 1000, message = "valid.place.description.size")
    private String description;
    @Size(max = 50, message = "valid.place.operationHour.size")
    private String operationHour;
}
