package com.tourism.configuration.annotation;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation"),
        @ApiResponse(responseCode = "201", description = "Successfully created"),
        @ApiResponse(responseCode = "400", description = "Error when logging in, for details see response errors"),
        @ApiResponse(responseCode = "406", description = "Not acceptable request"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
})
public @interface CommonApiResponses {}
