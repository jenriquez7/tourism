package com.tourism.dto.request;

import com.tourism.validation.ValidSortDirection;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;
import org.springframework.data.domain.Sort;
import org.springframework.validation.annotation.Validated;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Validated
public class PageableRequest {

    @Min(0)
    private Integer page = 0;

    @Min(1)
    @Max(100)
    private Integer size = 20;

    private String[] sort;

    @ValidSortDirection
    private Sort.Direction sortType = Sort.Direction.ASC;
}
