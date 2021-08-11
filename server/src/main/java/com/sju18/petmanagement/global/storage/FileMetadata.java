package com.sju18.petmanagement.global.storage;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FileMetadata {
    private String name;
    private Long size;
    private String entity;
    private String type;
    private String url;
}
