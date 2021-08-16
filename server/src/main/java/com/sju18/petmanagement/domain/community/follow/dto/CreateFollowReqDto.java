package com.sju18.petmanagement.domain.community.follow.dto;

import lombok.Data;

import javax.validation.constraints.PositiveOrZero;

@Data
public class CreateFollowReqDto {
    @PositiveOrZero(message = "valid.account.id.notNegative")
    private Long id;
}
