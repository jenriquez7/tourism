package com.tourism.test.controller;

import com.tourism.controller.LodgingOwnerController;
import com.tourism.dto.request.LodgingOwnerRequestDTO;
import com.tourism.dto.request.PageableRequest;
import com.tourism.dto.response.LodgingOwnerResponseDTO;
import com.tourism.dto.response.StandardResponseDto;
import com.tourism.infrastructure.JwtTokenProvider;
import com.tourism.model.Role;
import com.tourism.model.LodgingOwner;
import com.tourism.model.User;
import com.tourism.service.LodgingOwnerService;
import io.vavr.control.Either;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class LodgingOwnerControllerTests {

    @Mock
    private LodgingOwnerService service;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private LodgingOwnerController controller;


    private LodgingOwnerRequestDTO ownerRequestDTO;
    private LodgingOwnerResponseDTO ownerResponseDTO;
    private LodgingOwner tourist;
    private UUID lodgingId;

    @BeforeEach
    void setUp() {
        lodgingId = UUID.randomUUID();
        ownerRequestDTO = new LodgingOwnerRequestDTO("Poseedor", "Hotel", "photel@email.com", "12345678");
        ownerResponseDTO = new LodgingOwnerResponseDTO(lodgingId, "photel@email.com", "Poseedor", "Hotel");
        tourist = new LodgingOwner("photel@email.com", "12345678", "Poseedor", "Hotel", Role.LODGING_OWNER, true);
    }

    @Test
    @DisplayName("Create Lodging Owner")
    void create() {
        when(service.create(any(LodgingOwnerRequestDTO.class))).thenReturn(Either.right(ownerResponseDTO));
        ResponseEntity<StandardResponseDto<LodgingOwnerResponseDTO>> response = controller.create(request, ownerRequestDTO);

        verifyLodgingOwnerResponseDto(response);
        verify(service, times(1)).create(any(LodgingOwnerRequestDTO.class));
    }

    @Test
    @DisplayName("Find All Lodging Owner")
    void findAll() {
        List<LodgingOwnerResponseDTO> owners = Collections.singletonList(ownerResponseDTO);
        Page<LodgingOwnerResponseDTO> page = new PageImpl<>(owners);

        when(service.findAll(any(PageableRequest.class))).thenReturn(Either.right(page));

        PageableRequest pageableRequest = new PageableRequest(0, 10, new String[]{"id"}, Sort.Direction.ASC);
        ResponseEntity<StandardResponseDto<Page<LodgingOwnerResponseDTO>>> response = controller.findAll(request, pageableRequest);

        verifyPageLodgingOwnerResponseDto(response);

        verify(service, times(1)).findAll(pageableRequest);
    }

    @Test
    @DisplayName("Delete Lodging Owner By Admin")
    void deleteByAdmin() {
        when(service.delete(lodgingId)).thenReturn(Either.right(tourist));

        ResponseEntity<StandardResponseDto<LodgingOwner>> response = controller.deleteByAdmin(request, lodgingId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        StandardResponseDto<LodgingOwner> body = response.getBody();
        assertNotNull(body);

        Object[] data = body.getData();
        assertNotNull(data);
        assertEquals(1, data.length);
        assertInstanceOf(LodgingOwner.class, data[0]);
        assertEquals(tourist, data[0]);
        verify(service, times(1)).delete(lodgingId);
    }

    @Test
    @DisplayName("Delete Lodging Owner By Token")
    void delete() {
        User user = new User(lodgingId, "photel@email.com", Role.LODGING_OWNER);
        when(jwtTokenProvider.getUserFromToken(request)).thenReturn(user);
        when(service.delete(user.getId())).thenReturn(Either.right(tourist));

        ResponseEntity<StandardResponseDto<LodgingOwner>> response = controller.delete(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        StandardResponseDto<LodgingOwner> body = response.getBody();
        assertNotNull(body);

        Object[] data = body.getData();
        assertNotNull(data);
        assertEquals(1, data.length);
        assertInstanceOf(LodgingOwner.class, data[0]);
        assertEquals(tourist, data[0]);
        verify(service, times(1)).delete(user.getId());
    }

    @Test
    @DisplayName("Get Lodging Owner By Id")
    void getById() {
        when(service.getById(lodgingId)).thenReturn(Either.right(ownerResponseDTO));
        ResponseEntity<StandardResponseDto<LodgingOwnerResponseDTO>> response = controller.getById(request, lodgingId);
        verifyLodgingOwnerResponseDto(response);
        verify(service, times(1)).getById(lodgingId);
    }

    @Test
    @DisplayName("Get Tourist Profile")
    void profile() {
        User user = new User(lodgingId, "photel@email.com", Role.LODGING_OWNER);
        when(jwtTokenProvider.getUserFromToken(request)).thenReturn(user);
        when(service.getById(any(UUID.class))).thenReturn(Either.right(ownerResponseDTO));
        ResponseEntity<StandardResponseDto<LodgingOwnerResponseDTO>> response = controller.profile(request);
        verifyLodgingOwnerResponseDto(response);
        verify(service, times(1)).getById(lodgingId);
    }

    @Test
    @DisplayName("Get Tourist By Email")
    void findByEmail() {
        String email = "photel@email.com";
        List<LodgingOwnerResponseDTO> tourists = Collections.singletonList(ownerResponseDTO);
        Page<LodgingOwnerResponseDTO> page = new PageImpl<>(tourists);

        when(service.findByEmail(anyString(), any(PageableRequest.class))).thenReturn(Either.right(page));
        PageableRequest pageableRequest = new PageableRequest(0, 10, new String[]{"id"}, Sort.Direction.ASC);
        ResponseEntity<StandardResponseDto<Page<LodgingOwnerResponseDTO>>> response = controller.findByEmail(request, email, pageableRequest);

        verifyPageLodgingOwnerResponseDto(response);

        verify(service, times(1)).findByEmail(eq(email), argThat(req ->
                req.getPage() == 0 &&
                        req.getSize() == 10 &&
                        Arrays.equals(req.getSort(), new String[]{"id"}) &&
                        req.getSortType() == Sort.Direction.ASC
        ));
    }

    @Test
    @DisplayName("Find Tourist By Last Name")
    void findByLastName() {
        String lastName = "Verano";
        List<LodgingOwnerResponseDTO> tourists = Collections.singletonList(ownerResponseDTO);
        Page<LodgingOwnerResponseDTO> page = new PageImpl<>(tourists);

        when(service.findByLastName(anyString(), any(PageableRequest.class))).thenReturn(Either.right(page));
        PageableRequest pageableRequest = new PageableRequest(0, 10, new String[]{"id"}, Sort.Direction.ASC);
        ResponseEntity<StandardResponseDto<Page<LodgingOwnerResponseDTO>>> response = controller.findByLastName(request, lastName, pageableRequest);

        verifyPageLodgingOwnerResponseDto(response);

        verify(service, times(1)).findByLastName(eq(lastName), argThat(req ->
                req.getPage() == 0 &&
                        req.getSize() == 10 &&
                        Arrays.equals(req.getSort(), new String[]{"id"}) &&
                        req.getSortType() == Sort.Direction.ASC
        ));
    }



    private void verifyLodgingOwnerResponseDto(ResponseEntity<StandardResponseDto<LodgingOwnerResponseDTO>> response) {
        assertEquals(HttpStatus.OK, response.getStatusCode());
        StandardResponseDto<LodgingOwnerResponseDTO> body = response.getBody();
        assertNotNull(body);

        Object[] data = body.getData();
        assertNotNull(data);
        assertEquals(1, data.length);

        assertInstanceOf(LodgingOwnerResponseDTO.class, data[0]);
        LodgingOwnerResponseDTO resultAdmin = (LodgingOwnerResponseDTO) data[0];
        assertEquals(ownerResponseDTO.email(), resultAdmin.email());
        assertEquals(ownerResponseDTO.id(), resultAdmin.id());
    }

    private void verifyPageLodgingOwnerResponseDto(ResponseEntity<StandardResponseDto<Page<LodgingOwnerResponseDTO>>> response) {
        assertEquals(HttpStatus.OK, response.getStatusCode());
        StandardResponseDto<Page<LodgingOwnerResponseDTO>> body = response.getBody();
        assertNotNull(body);

        Object[] data = body.getData();
        assertNotNull(data);
        assertEquals(1, data.length);
        assertInstanceOf(Page.class, data[0]);

        Page<LodgingOwnerResponseDTO> resultPage = (Page<LodgingOwnerResponseDTO>) data[0];
        assertEquals(1, resultPage.getTotalElements());
        assertEquals(1, resultPage.getContent().size());

        LodgingOwnerResponseDTO resultTourist = resultPage.getContent().getFirst();
        assertEquals(ownerResponseDTO.id(), resultTourist.id());
        assertEquals(ownerResponseDTO.email(), resultTourist.email());
    }
}
