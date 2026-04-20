package com.yumaste.yumasteapi.DTO.response;

import java.io.Serializable;
import java.util.List;
import org.springframework.data.domain.Page;

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