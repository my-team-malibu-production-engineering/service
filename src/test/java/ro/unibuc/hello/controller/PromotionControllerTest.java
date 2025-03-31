package ro.unibuc.hello.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;
import ro.unibuc.hello.data.promotions.PromotionEntity;
import ro.unibuc.hello.service.PromotionService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class PromotionControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PromotionService promotionService;

    @InjectMocks
    private PromotionController promotionController;

    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(promotionController).build();
        
        // Configurare ObjectMapper pentru a gestiona serializarea/deserializarea LocalDateTime
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    public void testCreatePromotion_Success() throws Exception {
        PromotionEntity promotionToCreate = createMockPromotion();
        PromotionEntity createdPromotion = createMockPromotion();
        createdPromotion.setId("promo123");

        when(promotionService.createPromotion(any(PromotionEntity.class))).thenReturn(createdPromotion);

        mockMvc.perform(post("/api/promotions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(promotionToCreate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("promo123")))
                .andExpect(jsonPath("$.name", is("Summer Sale")))
                .andExpect(jsonPath("$.type", is("PERCENT_DISCOUNT")));

        verify(promotionService, times(1)).createPromotion(any(PromotionEntity.class));
    }

    @Test
    public void testCreatePromotion_ServerError() throws Exception {
        PromotionEntity promotionToCreate = createMockPromotion();

        when(promotionService.createPromotion(any(PromotionEntity.class)))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(post("/api/promotions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(promotionToCreate)))
                .andExpect(status().isInternalServerError())
                .andExpect(status().reason("Service not available"));

        verify(promotionService, times(1)).createPromotion(any(PromotionEntity.class));
    }

    @Test
    public void testGetPromotionById_Success() throws Exception {
        String id = "promo123";
        PromotionEntity promotion = createMockPromotion();
        promotion.setId(id);

        when(promotionService.getPromotionById(id)).thenReturn(promotion);

        mockMvc.perform(get("/api/promotions/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(id)))
                .andExpect(jsonPath("$.name", is("Summer Sale")))
                .andExpect(jsonPath("$.active", is(true)));

        verify(promotionService, times(1)).getPromotionById(id);
    }

    @Test
    public void testGetPromotionById_NotFound() throws Exception {
        String id = "nonexistent";

        when(promotionService.getPromotionById(id))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

        mockMvc.perform(get("/api/promotions/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(status().reason("Promotion not found"));

        verify(promotionService, times(1)).getPromotionById(id);
    }

    @Test
    public void testGetAllPromotions_Success() throws Exception {
        List<PromotionEntity> promotions = Arrays.asList(
                createMockPromotionWithId("promo1", "Summer Sale", true),
                createMockPromotionWithId("promo2", "Black Friday", false)
        );

        when(promotionService.getAllPromotions()).thenReturn(promotions);

        mockMvc.perform(get("/api/promotions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is("promo1")))
                .andExpect(jsonPath("$[0].name", is("Summer Sale")))
                .andExpect(jsonPath("$[1].id", is("promo2")))
                .andExpect(jsonPath("$[1].name", is("Black Friday")));

        verify(promotionService, times(1)).getAllPromotions();
    }

    @Test
    public void testGetAllPromotions_ServerError() throws Exception {
        when(promotionService.getAllPromotions())
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/promotions"))
                .andExpect(status().isInternalServerError())
                .andExpect(status().reason("Service not available"));

        verify(promotionService, times(1)).getAllPromotions();
    }

    @Test
    public void testGetActivePromotions_Success() throws Exception {
        List<PromotionEntity> activePromotions = Arrays.asList(
                createMockPromotionWithId("promo1", "Summer Sale", true),
                createMockPromotionWithId("promo3", "Holiday Special", true)
        );

        when(promotionService.getActivePromotions()).thenReturn(activePromotions);

        mockMvc.perform(get("/api/promotions/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is("promo1")))
                .andExpect(jsonPath("$[0].active", is(true)))
                .andExpect(jsonPath("$[1].id", is("promo3")))
                .andExpect(jsonPath("$[1].active", is(true)));

        verify(promotionService, times(1)).getActivePromotions();
    }

    @Test
    public void testUpdatePromotion_Success() throws Exception {
        String id = "promo123";
        PromotionEntity updatedPromotion = createMockPromotion();
        updatedPromotion.setId(id);
        updatedPromotion.setName("Updated Summer Sale");
        updatedPromotion.setDiscountValue(25.0);

        when(promotionService.updatePromotion(eq(id), any(PromotionEntity.class)))
                .thenReturn(updatedPromotion);

        mockMvc.perform(put("/api/promotions/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedPromotion)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(id)))
                .andExpect(jsonPath("$.name", is("Updated Summer Sale")))
                .andExpect(jsonPath("$.discountValue", is(25.0)));

        verify(promotionService, times(1)).updatePromotion(eq(id), any(PromotionEntity.class));
    }

    @Test
    public void testUpdatePromotion_NotFound() throws Exception {
        String id = "nonexistent";
        PromotionEntity updatedPromotion = createMockPromotion();
        updatedPromotion.setId(id);

        when(promotionService.updatePromotion(eq(id), any(PromotionEntity.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

        mockMvc.perform(put("/api/promotions/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedPromotion)))
                .andExpect(status().isNotFound())
                .andExpect(status().reason("Promotion not found"));

        verify(promotionService, times(1)).updatePromotion(eq(id), any(PromotionEntity.class));
    }

    @Test
    public void testDeletePromotion_Success() throws Exception {
        String id = "promo123";

        doNothing().when(promotionService).deletePromotion(id);

        mockMvc.perform(delete("/api/promotions/{id}", id))
                .andExpect(status().isNoContent());

        verify(promotionService, times(1)).deletePromotion(id);
    }

    @Test
    public void testDeletePromotion_NotFound() throws Exception {
        String id = "nonexistent";

        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND))
                .when(promotionService).deletePromotion(id);

        mockMvc.perform(delete("/api/promotions/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(status().reason("Promotion not found"));

        verify(promotionService, times(1)).deletePromotion(id);
    }

    @Test
    public void testActivatePromotion_Success() throws Exception {
        String id = "promo123";
        PromotionEntity promotion = createMockPromotion();
        promotion.setId(id);
        promotion.setActive(false); // Inițial inactivă

        PromotionEntity activatedPromotion = createMockPromotion();
        activatedPromotion.setId(id);
        activatedPromotion.setActive(true); // După activare

        when(promotionService.getPromotionById(id)).thenReturn(promotion);
        when(promotionService.updatePromotion(eq(id), any(PromotionEntity.class)))
                .thenReturn(activatedPromotion);

        mockMvc.perform(put("/api/promotions/{id}/activate", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(id)))
                .andExpect(jsonPath("$.active", is(true)));

        verify(promotionService, times(1)).getPromotionById(id);
        verify(promotionService, times(1)).updatePromotion(eq(id), any(PromotionEntity.class));
    }

    @Test
    public void testActivatePromotion_NotFound() throws Exception {
        String id = "nonexistent";

        when(promotionService.getPromotionById(id))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

        mockMvc.perform(put("/api/promotions/{id}/activate", id))
                .andExpect(status().isNotFound())
                .andExpect(status().reason("Promotion not found"));

        verify(promotionService, times(1)).getPromotionById(id);
        verify(promotionService, never()).updatePromotion(anyString(), any(PromotionEntity.class));
    }

    @Test
    public void testDeactivatePromotion_Success() throws Exception {
        String id = "promo123";
        PromotionEntity promotion = createMockPromotion();
        promotion.setId(id);
        promotion.setActive(true); // Inițial activă

        PromotionEntity deactivatedPromotion = createMockPromotion();
        deactivatedPromotion.setId(id);
        deactivatedPromotion.setActive(false); // După dezactivare

        when(promotionService.getPromotionById(id)).thenReturn(promotion);
        when(promotionService.updatePromotion(eq(id), any(PromotionEntity.class)))
                .thenReturn(deactivatedPromotion);

        mockMvc.perform(put("/api/promotions/{id}/deactivate", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(id)))
                .andExpect(jsonPath("$.active", is(false)));

        verify(promotionService, times(1)).getPromotionById(id);
        verify(promotionService, times(1)).updatePromotion(eq(id), any(PromotionEntity.class));
    }

    @Test
    public void testDeactivatePromotion_NotFound() throws Exception {
        String id = "nonexistent";

        when(promotionService.getPromotionById(id))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

        mockMvc.perform(put("/api/promotions/{id}/deactivate", id))
                .andExpect(status().isNotFound())
                .andExpect(status().reason("Promotion not found"));

        verify(promotionService, times(1)).getPromotionById(id);
        verify(promotionService, never()).updatePromotion(anyString(), any(PromotionEntity.class));
    }

    // Metode helper pentru crearea obiectelor mock pentru teste
    private PromotionEntity createMockPromotion() {
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusDays(30);
        String[] categories = {"Electronics", "Appliances"};

        return new PromotionEntity(
                "Summer Sale",
                "20% off on all electronics",
                PromotionEntity.PromotionType.PERCENT_DISCOUNT,
                0, 0, 20.0,
                startDate,
                endDate,
                true,
                categories
        );
    }

    private PromotionEntity createMockPromotionWithId(String id, String name, boolean active) {
        PromotionEntity promotion = createMockPromotion();
        promotion.setId(id);
        promotion.setName(name);
        promotion.setActive(active);
        return promotion;
    }
}