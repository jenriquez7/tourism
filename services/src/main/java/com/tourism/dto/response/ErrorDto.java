package com.tourism.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springframework.http.HttpStatus;

import java.io.Serial;
import java.io.Serializable;

@Schema(description = "Detail error in case of an HTTP error response")
@Builder
@Setter
@Getter
@RequiredArgsConstructor
public class ErrorDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1905122041950251207L;

    @Schema(description = "Error code")
    @NonNull
    private HttpStatus code;

    @Schema(description = "Error message")
    @NonNull
    private String message;

    @Schema(description = "Detail of the error in case of requesting to send an exception message")
    private String detail;

    public ErrorDto(@NonNull HttpStatus code, @NonNull String message, String detail) {
        this.code = code;
        this.message = message;
        this.detail = detail;
    }
}
