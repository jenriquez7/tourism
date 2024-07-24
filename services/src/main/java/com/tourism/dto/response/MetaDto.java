package com.tourism.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "The metadata response")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MetaDto {
    @Schema(description = "The HTTP method request")
    private String method;

    @Schema(description = "The HTTP operation request")
    private String operation;

    @Schema(description = "Pagination in case of paged response")
    private PagingDto paging;
}
