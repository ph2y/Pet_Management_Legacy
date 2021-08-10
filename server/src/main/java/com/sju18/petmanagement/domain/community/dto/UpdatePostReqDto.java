package com.sju18.petmanagement.domain.community.dto;

import lombok.Data;

import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

@Data
public class UpdatePostReqDto {
    @PositiveOrZero(message = "valid.post.id.notNegative")
    private Long id;
    @PositiveOrZero(message = "valid.post.petId.notNegative")
    private Long petId;
    @NotBlank(message = "valid.post.contents.blank")
    @Size(max = 10000, message = "valid.post.contents.size")
    private String contents;
    @Size(max = 5, message = "valid.post.hashTags.count")
    private List<@Size(max = 20, message = "valid.post.hashTags.size") String> hashTags;
    @Pattern(
            regexp = "^(PUBLIC|PRIVATE|FRIEND)$",
            message = "valid.post.disclosure.enum"
    )
    private String disclosure;
    @DecimalMax(value = "90.0", message = "valid.post.geoTagLat.max")
    @DecimalMin(value = "-90.0", message = "valid.post.geoTagLat.min")
    private BigDecimal geoTagLat;
    @DecimalMax(value = "180.0", message = "valid.post.geoTagLong.max")
    @DecimalMin(value = "-180.0", message = "valid.post.geoTagLong.min")
    private BigDecimal geoTagLong;
}
