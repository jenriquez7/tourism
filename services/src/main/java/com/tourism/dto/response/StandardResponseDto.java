package com.tourism.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "Standard Response for all project's microservice")
@Data
public class StandardResponseDto<T> {

    @JsonIgnore
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private ObjectMapper mapper = new ObjectMapper();

    @Schema(description = "The metadata response")
    private MetaDto meta;

    @Schema(description = "Data response")
    private T[] data;

    @Schema(description = "Details errors in case of an HTTP error response")
    private ErrorDto[] errors;

    @JsonIgnore
    public String toString() {
        try {
            return mapper.writeValueAsString(this);
        } catch (Exception e) {
            return "";
        }
    }
}
