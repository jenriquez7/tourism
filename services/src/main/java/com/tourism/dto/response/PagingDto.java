package com.tourism.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Schema(description = "Pagination in case of paged response")
@ToString
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagingDto {

    @Schema(description = "Page size")
    private Integer size;

    @Schema(description = "Page offset")
    private Integer offset;
}

