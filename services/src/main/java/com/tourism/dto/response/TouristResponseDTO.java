package com.tourism.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Tourist response DTO")
public record TouristResponseDTO (UUID id, String email, String firstName, String lastName) { }
