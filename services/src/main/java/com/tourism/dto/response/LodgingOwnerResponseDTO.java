package com.tourism.dto.response;

import java.util.UUID;

public record LodgingOwnerResponseDTO (UUID id, String email, String firstName, String lastName) { }
