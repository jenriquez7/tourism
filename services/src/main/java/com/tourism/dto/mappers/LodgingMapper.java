package com.tourism.dto.mappers;

import com.tourism.dto.request.LodgingRequestDTO;
import com.tourism.dto.response.LodgingResponseDTO;
import com.tourism.model.Lodging;
import com.tourism.model.LodgingOwner;
import com.tourism.model.TouristicPlace;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {TouristicPlaceMapper.class})
public interface LodgingMapper {

    @Mapping(target = "touristicPlace", source = "touristicPlace")
    LodgingResponseDTO modelToResponseDto(Lodging lodging);

    @Mapping(target = "touristicPlace", source = "touristicPlace")
    @Mapping(target = "name", source = "requestDto.name")
    @Mapping(target = "description", source = "requestDto.description")
    @Mapping(target = "information", source = "requestDto.information")
    @Mapping(target = "nightPrice", source = "requestDto.nightPrice")
    @Mapping(target = "phone", source = "requestDto.phone")
    @Mapping(target = "capacity", source = "requestDto.capacity")
    @Mapping(target = "lodgingOwner", source = "owner")
    @Mapping(target = "enabled", source = "enabled")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    Lodging requestDtoToModel(LodgingRequestDTO requestDto, TouristicPlace touristicPlace, LodgingOwner owner, boolean enabled);
}
