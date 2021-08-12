package com.sju18.petmanagement.domain.community.dto;

import lombok.Data;

import javax.validation.constraints.PositiveOrZero;

@Data
public class DeleteFollowReqDto {
    @PositiveOrZero(message = "valid.account.id.notNegative")
    private Long id;
}
