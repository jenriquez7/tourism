package com.tourism.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.springframework.data.domain.Sort;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageableRequest {

    @Min(0)
    private Integer page = 0;

    @Min(1)
    @Max(100)
    private Integer size = 20;

    private String[] sort;

    @Pattern(regexp = "^(ASC|DESC)$", message = "Sort type must be either ASC or DESC")
    private Sort.Direction sortType = Sort.Direction.ASC;
}
