package com.sju18.petmanagement.domain.map.review.dto;

import com.sju18.petmanagement.domain.map.review.dao.Review;
import com.sju18.petmanagement.global.common.DtoMetadata;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class FetchReviewResDto {
    private DtoMetadata _metadata;
    private List<Review> reviewList;
}
