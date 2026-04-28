package com.yumaste.yumasteapi.DTO.response;

import org.springframework.data.domain.Page;

import java.io.Serializable;
import java.util.List;

public record PagedResponseDTO<T>(
        List<T> content,
        int pageNumber,
        int pageSize,
        long totalElements,
        int totalPages,
        boolean last
) implements Serializable {

    public PagedResponseDTO(Page<T> page) {
        this(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );

    }
}