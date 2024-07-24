package com.tourism.dto.request;

import com.tourism.model.Category;
import com.tourism.model.Region;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Touristic place DTO")
public class TouristicPlaceRequestDTO {

    @Schema(description = "id, for updates")
    private UUID id;

    @Schema(example = "Cabo polonio", description = "touristic place name")
    @Size(max = 100, message = "{validation.name.size.too_long}")
    @NotNull
    private String name;

    @Schema(example = "Un lugar de mucha naturaleza...", description = "description of the touristic place")
    @Size(max = 500, message = "{validation.description.size.too_long}")
    @NotNull
    private String description;

    @Schema(description = "Where the touristic place is")
    @NotNull
    private Region region;

    @Schema(description = "Touristic place categories")
    @NotNull
    private List<Category> categories;

    @Schema(description = "enabled")
    @NotNull
    private Boolean enabled;

}
