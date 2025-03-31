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
import ro.unibuc.hello.data.loyalty.LoyaltyCardEntity;
import ro.unibuc.hello.service.LoyaltyCardService;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class LoyaltyCardControllerTest {

    private MockMvc mockMvc;

    @Mock
    private LoyaltyCardService loyaltyCardService;

    @InjectMocks
    private LoyaltyCardController loyaltyCardController;

    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(loyaltyCardController).build();
        
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // Pentru serializarea LocalDate
    }

    @Test
    public void testIssueCard_Success() throws Exception {
        String userId = "user123";
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("cardType", "GOLD");

        LoyaltyCardEntity mockCard = createMockCard(userId, LoyaltyCardEntity.CardType.GOLD);
        
        when(loyaltyCardService.issueCard(eq(userId), eq(LoyaltyCardEntity.CardType.GOLD)))
            .thenReturn(mockCard);

        mockMvc.perform(post("/api/loyalty-cards/user/{userId}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("card123")))
                .andExpect(jsonPath("$.cardType", is("GOLD")))
                .andExpect(jsonPath("$.userId", is(userId)));

        verify(loyaltyCardService, times(1)).issueCard(eq(userId), eq(LoyaltyCardEntity.CardType.GOLD));
    }

    @Test
    public void testIssueCard_InvalidCardType() throws Exception {
        String userId = "user123";
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("cardType", "INVALID_TYPE");

        mockMvc.perform(post("/api/loyalty-cards/user/{userId}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(status().reason("Invalid card type"));

        verify(loyaltyCardService, never()).issueCard(anyString(), any(LoyaltyCardEntity.CardType.class));
    }

    @Test
    public void testIssueCard_UserNotFound() throws Exception {
        String userId = "nonexistent";
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("cardType", "BRONZE");

        when(loyaltyCardService.issueCard(eq(userId), any(LoyaltyCardEntity.CardType.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

        mockMvc.perform(post("/api/loyalty-cards/user/{userId}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isNotFound())
                .andExpect(status().reason("User not found"));

        verify(loyaltyCardService, times(1)).issueCard(eq(userId), any(LoyaltyCardEntity.CardType.class));
    }

    @Test
    public void testIssueCard_ServerError() throws Exception {
        String userId = "user123";
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("cardType", "PREMIUM");

        when(loyaltyCardService.issueCard(eq(userId), any(LoyaltyCardEntity.CardType.class)))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(post("/api/loyalty-cards/user/{userId}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isInternalServerError())
                .andExpect(status().reason("Service not available"));

        verify(loyaltyCardService, times(1)).issueCard(eq(userId), any(LoyaltyCardEntity.CardType.class));
    }

    @Test
    public void testGetCardById_Success() throws Exception {
        String cardId = "card123";
        LoyaltyCardEntity mockCard = createMockCard("user123", LoyaltyCardEntity.CardType.BRONZE);
        
        when(loyaltyCardService.getCardById(cardId)).thenReturn(mockCard);

        mockMvc.perform(get("/api/loyalty-cards/{id}", cardId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("card123")))
                .andExpect(jsonPath("$.cardType", is("BRONZE")))
                .andExpect(jsonPath("$.points", is(100)));

        verify(loyaltyCardService, times(1)).getCardById(cardId);
    }

    @Test
    public void testGetCardById_NotFound() throws Exception {
        String cardId = "nonexistent";
        
        when(loyaltyCardService.getCardById(cardId))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

        mockMvc.perform(get("/api/loyalty-cards/{id}", cardId))
                .andExpect(status().isNotFound())
                .andExpect(status().reason("Card not found"));

        verify(loyaltyCardService, times(1)).getCardById(cardId);
    }

    @Test
    public void testGetCardsByUser_Success() throws Exception {
        String userId = "user123";
        List<LoyaltyCardEntity> mockCards = Arrays.asList(
            createMockCard(userId, LoyaltyCardEntity.CardType.BRONZE),
            createMockCard(userId, LoyaltyCardEntity.CardType.GOLD)
        );
        
        when(loyaltyCardService.getCardsByUser(userId)).thenReturn(mockCards);

        mockMvc.perform(get("/api/loyalty-cards/user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].cardType", is("BRONZE")))
                .andExpect(jsonPath("$[1].cardType", is("GOLD")));

        verify(loyaltyCardService, times(1)).getCardsByUser(userId);
    }

    @Test
    public void testGetCardsByUser_UserNotFound() throws Exception {
        String userId = "nonexistent";
        
        when(loyaltyCardService.getCardsByUser(userId))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

        mockMvc.perform(get("/api/loyalty-cards/user/{userId}", userId))
                .andExpect(status().isNotFound())
                .andExpect(status().reason("User not found"));

        verify(loyaltyCardService, times(1)).getCardsByUser(userId);
    }

    @Test
    public void testUpgradeCard_Success() throws Exception {
        String cardId = "card123";
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("newType", "PREMIUM");

        LoyaltyCardEntity mockUpgradedCard = createMockCard("user123", LoyaltyCardEntity.CardType.PREMIUM);
        mockUpgradedCard.setDiscountPercentage(15.0);
        
        when(loyaltyCardService.upgradeCard(eq(cardId), eq(LoyaltyCardEntity.CardType.PREMIUM)))
                .thenReturn(mockUpgradedCard);

        mockMvc.perform(put("/api/loyalty-cards/{id}/upgrade", cardId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cardType", is("PREMIUM")))
                .andExpect(jsonPath("$.discountPercentage", is(15.0)));

        verify(loyaltyCardService, times(1)).upgradeCard(eq(cardId), eq(LoyaltyCardEntity.CardType.PREMIUM));
    }

    @Test
    public void testUpgradeCard_InvalidCardType() throws Exception {
        String cardId = "card123";
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("newType", "INVALID_TYPE");

        mockMvc.perform(put("/api/loyalty-cards/{id}/upgrade", cardId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(status().reason("Invalid card type"));

        verify(loyaltyCardService, never()).upgradeCard(anyString(), any(LoyaltyCardEntity.CardType.class));
    }

    @Test
    public void testUpgradeCard_CardNotFound() throws Exception {
        String cardId = "nonexistent";
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("newType", "GOLD");

        when(loyaltyCardService.upgradeCard(eq(cardId), any(LoyaltyCardEntity.CardType.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

        mockMvc.perform(put("/api/loyalty-cards/{id}/upgrade", cardId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isNotFound())
                .andExpect(status().reason("Card not found"));

        verify(loyaltyCardService, times(1)).upgradeCard(eq(cardId), any(LoyaltyCardEntity.CardType.class));
    }

    @Test
    public void testAddPoints_Success() throws Exception {
        String cardId = "card123";
        Map<String, Integer> requestBody = new HashMap<>();
        requestBody.put("points", 50);

        LoyaltyCardEntity mockCard = createMockCard("user123", LoyaltyCardEntity.CardType.BRONZE);
        mockCard.setPoints(150); // 100 initial + 50 added
        
        when(loyaltyCardService.addPoints(eq(cardId), eq(50))).thenReturn(mockCard);

        mockMvc.perform(post("/api/loyalty-cards/{id}/points", cardId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.points", is(150)));

        verify(loyaltyCardService, times(1)).addPoints(eq(cardId), eq(50));
    }

   

    @Test
    public void testAddPoints_CardNotFound() throws Exception {
        String cardId = "nonexistent";
        Map<String, Integer> requestBody = new HashMap<>();
        requestBody.put("points", 50);

        when(loyaltyCardService.addPoints(eq(cardId), anyInt()))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

        mockMvc.perform(post("/api/loyalty-cards/{id}/points", cardId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isNotFound())
                .andExpect(status().reason("Card not found"));

        verify(loyaltyCardService, times(1)).addPoints(eq(cardId), anyInt());
    }

    @Test
    public void testCalculateDiscount_Success() throws Exception {
        String cardId = "card123";
        double amount = 100.0;
        double expectedDiscount = 10.0; // 10% of 100
        
        when(loyaltyCardService.calculateDiscount(eq(cardId), eq(amount))).thenReturn(expectedDiscount);

        mockMvc.perform(get("/api/loyalty-cards/{id}/discount", cardId)
                .param("amount", String.valueOf(amount)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.original", is(100.0)))
                .andExpect(jsonPath("$.discount", is(10.0)))
                .andExpect(jsonPath("$.final", is(90.0)));

        verify(loyaltyCardService, times(1)).calculateDiscount(eq(cardId), eq(amount));
    }

    @Test
    public void testCalculateDiscount_CardNotFound() throws Exception {
        String cardId = "nonexistent";
        double amount = 100.0;
        
        when(loyaltyCardService.calculateDiscount(eq(cardId), anyDouble()))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

        mockMvc.perform(get("/api/loyalty-cards/{id}/discount", cardId)
                .param("amount", String.valueOf(amount)))
                .andExpect(status().isNotFound())
                .andExpect(status().reason("Card not found"));

        verify(loyaltyCardService, times(1)).calculateDiscount(eq(cardId), anyDouble());
    }

    @Test
    public void testDeleteCard_Success() throws Exception {
        String cardId = "card123";
        
        doNothing().when(loyaltyCardService).deleteCard(cardId);

        mockMvc.perform(delete("/api/loyalty-cards/{id}", cardId))
                .andExpect(status().isNoContent());

        verify(loyaltyCardService, times(1)).deleteCard(cardId);
    }

    @Test
    public void testDeleteCard_CardNotFound() throws Exception {
        String cardId = "nonexistent";
        
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND))
                .when(loyaltyCardService).deleteCard(cardId);

        mockMvc.perform(delete("/api/loyalty-cards/{id}", cardId))
                .andExpect(status().isNotFound())
                .andExpect(status().reason("Card not found"));

        verify(loyaltyCardService, times(1)).deleteCard(cardId);
    }

    @Test
    public void testDeleteCard_ServerError() throws Exception {
        String cardId = "card123";
        
        doThrow(new RuntimeException("Database error"))
                .when(loyaltyCardService).deleteCard(cardId);

        mockMvc.perform(delete("/api/loyalty-cards/{id}", cardId))
                .andExpect(status().isInternalServerError())
                .andExpect(status().reason("Service not available"));

        verify(loyaltyCardService, times(1)).deleteCard(cardId);
    }

    // Metodă helper pentru a crea carduri mock pentru teste
    private LoyaltyCardEntity createMockCard(String userId, LoyaltyCardEntity.CardType cardType) {
        LoyaltyCardEntity card = new LoyaltyCardEntity(cardType, userId);
        card.setId("card123");
        card.setPoints(100);
        card.setIssueDate(LocalDate.now());
        card.setExpiryDate(LocalDate.now().plusYears(2));
        
        // Set discount based on card type
        switch (cardType) {
            case BRONZE:
                card.setDiscountPercentage(5.0);
                break;
            case GOLD:
                card.setDiscountPercentage(10.0);
                break;
            case PREMIUM:
                card.setDiscountPercentage(15.0);
                break;
        }
        
        return card;
    }
}