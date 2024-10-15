package com.tourism.dto.mappers;

import com.tourism.dto.response.BookingResponseDTO;
import com.tourism.model.Booking;
import com.tourism.model.Lodging;
import com.tourism.model.Tourist;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    @Mapping(target = "lodgingName", source = "lodging.name")
    @Mapping(target = "firstName", source = "tourist.firstName")
    @Mapping(target = "lastName", source = "tourist.lastName")
    @Mapping(target = "lodgingPhone", source = "lodging.phone")
    @Mapping(target = "lodgingInformation", source = "lodging.information")
    BookingResponseDTO modelToResponseDTO(Booking booking);

    default String map(Lodging lodging) {
        return lodging != null ? lodging.getName() : null;
    }

    default String mapPhone(Lodging lodging) {
        return lodging != null ? lodging.getPhone() : null;
    }

    default String mapInformation(Lodging lodging) {
        return lodging != null ? lodging.getInformation() : null;
    }

    default String mapFirstName(Tourist tourist) {
        return tourist != null ? tourist.getFirstName() : null;
    }

    default String mapLastName(Tourist tourist) {
        return tourist != null ? tourist.getLastName() : null;
    }
}
