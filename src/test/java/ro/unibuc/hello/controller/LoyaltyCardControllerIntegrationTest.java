package ro.unibuc.hello.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ro.unibuc.hello.data.loyalty.LoyaltyCardEntity;
import ro.unibuc.hello.data.loyalty.LoyaltyCardRepository;
import ro.unibuc.hello.service.LoyaltyCardService;

import java.time.LocalDate;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class LoyaltyCardControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LoyaltyCardService loyaltyCardService;

    @MockBean
    private LoyaltyCardRepository loyaltyCardRepository;

    @Test
    public void testIssueCard() throws Exception {
        // Arrange
        String userId = "test-user-id";
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("cardType", "GOLD");

        LoyaltyCardEntity newCard = new LoyaltyCardEntity(LoyaltyCardEntity.CardType.GOLD, userId);
        newCard.setId("new-card-id");
        newCard.setPoints(0);
        newCard.setIssueDate(LocalDate.now());
        newCard.setExpiryDate(LocalDate.now().plusYears(2));
        newCard.setDiscountPercentage(10.0);

        when(loyaltyCardService.issueCard(eq(userId), eq(LoyaltyCardEntity.CardType.GOLD)))
            .thenReturn(newCard);

        // Act & Assert
        mockMvc.perform(post("/api/loyalty-cards/user/{userId}", userId)
               .contentType(MediaType.APPLICATION_JSON)
               .content(objectMapper.writeValueAsString(requestBody)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id").value("new-card-id"))
               .andExpect(jsonPath("$.cardType").value("GOLD"))
               .andExpect(jsonPath("$.userId").value(userId))
               .andExpect(jsonPath("$.points").value(0))
               .andExpect(jsonPath("$.discountPercentage").value(10.0));
    }

    @Test
    public void testGetCardById() throws Exception {
        // Arrange
        String cardId = "test-card-id";
        LoyaltyCardEntity card = new LoyaltyCardEntity(LoyaltyCardEntity.CardType.BRONZE, "user-id");
        card.setId(cardId);
        card.setPoints(100);
        card.setIssueDate(LocalDate.now().minusMonths(6));
        card.setExpiryDate(LocalDate.now().plusYears(1).plusMonths(6));
        card.setDiscountPercentage(5.0);

        when(loyaltyCardService.getCardById(cardId)).thenReturn(card);

        // Act & Assert
        mockMvc.perform(get("/api/loyalty-cards/{id}", cardId))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id").value(cardId))
               .andExpect(jsonPath("$.cardType").value("BRONZE"))
               .andExpect(jsonPath("$.userId").value("user-id"))
               .andExpect(jsonPath("$.points").value(100))
               .andExpect(jsonPath("$.discountPercentage").value(5.0));
    }

    @Test
    public void testGetCardsByUser() throws Exception {
        // Arrange
        String userId = "test-user-id";
        LoyaltyCardEntity card1 = new LoyaltyCardEntity(LoyaltyCardEntity.CardType.BRONZE, userId);
        card1.setId("card-1");
        card1.setPoints(50);

        LoyaltyCardEntity card2 = new LoyaltyCardEntity(LoyaltyCardEntity.CardType.GOLD, userId);
        card2.setId("card-2");
        card2.setPoints(200);

        List<LoyaltyCardEntity> userCards = Arrays.asList(card1, card2);

        when(loyaltyCardService.getCardsByUser(userId)).thenReturn(userCards);

        // Act & Assert
        mockMvc.perform(get("/api/loyalty-cards/user/{userId}", userId))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$[0].id").value("card-1"))
               .andExpect(jsonPath("$[0].cardType").value("BRONZE"))
               .andExpect(jsonPath("$[1].id").value("card-2"))
               .andExpect(jsonPath("$[1].cardType").value("GOLD"))
               .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    public void testUpgradeCard() throws Exception {
        // Arrange
        String cardId = "card-to-upgrade";
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("newType", "PREMIUM");

        LoyaltyCardEntity upgradedCard = new LoyaltyCardEntity(LoyaltyCardEntity.CardType.PREMIUM, "user-id");
        upgradedCard.setId(cardId);
        upgradedCard.setPoints(300);
        upgradedCard.setDiscountPercentage(15.0);
        upgradedCard.setIssueDate(LocalDate.now().minusMonths(6));
        upgradedCard.setExpiryDate(LocalDate.now().plusYears(2)); // Data extinsă după upgrade

        when(loyaltyCardService.upgradeCard(eq(cardId), eq(LoyaltyCardEntity.CardType.PREMIUM)))
            .thenReturn(upgradedCard);

        // Act & Assert
        mockMvc.perform(put("/api/loyalty-cards/{id}/upgrade", cardId)
               .contentType(MediaType.APPLICATION_JSON)
               .content(objectMapper.writeValueAsString(requestBody)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id").value(cardId))
               .andExpect(jsonPath("$.cardType").value("PREMIUM"))
               .andExpect(jsonPath("$.discountPercentage").value(15.0));
    }

    @Test
    public void testAddPoints() throws Exception {
        // Arrange
        String cardId = "card-id";
        Map<String, Integer> requestBody = new HashMap<>();
        requestBody.put("points", 50);

        LoyaltyCardEntity updatedCard = new LoyaltyCardEntity(LoyaltyCardEntity.CardType.GOLD, "user-id");
        updatedCard.setId(cardId);
        updatedCard.setPoints(250); // 200 existente + 50 noi
        updatedCard.setDiscountPercentage(10.0);

        when(loyaltyCardService.addPoints(eq(cardId), eq(50)))
            .thenReturn(updatedCard);

        // Act & Assert
        mockMvc.perform(post("/api/loyalty-cards/{id}/points", cardId)
               .contentType(MediaType.APPLICATION_JSON)
               .content(objectMapper.writeValueAsString(requestBody)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id").value(cardId))
               .andExpect(jsonPath("$.points").value(250));
    }

    @Test
    public void testCalculateDiscount() throws Exception {
        // Arrange
        String cardId = "card-id";
        double amount = 200.0;
        double discount = 20.0; // 10% din 200

        when(loyaltyCardService.calculateDiscount(cardId, amount))
            .thenReturn(discount);

        // Act & Assert
        mockMvc.perform(get("/api/loyalty-cards/{id}/discount", cardId)
               .param("amount", String.valueOf(amount)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.original").value(200.0))
               .andExpect(jsonPath("$.discount").value(20.0))
               .andExpect(jsonPath("$.final").value(180.0));
    }

    @Test
    public void testDeleteCard() throws Exception {
        // Arrange
        String cardId = "card-to-delete";
        doNothing().when(loyaltyCardService).deleteCard(cardId);

        // Act & Assert
        mockMvc.perform(delete("/api/loyalty-cards/{id}", cardId))
               .andExpect(status().isNoContent());
    }

    @Test
    public void testGetCardById_NotFound() throws Exception {
        // Arrange
        String nonExistentId = "non-existent";
        when(loyaltyCardService.getCardById(nonExistentId))
            .thenThrow(new Exception("404 NOT_FOUND"));

        // Act & Assert
        mockMvc.perform(get("/api/loyalty-cards/{id}", nonExistentId))
               .andExpect(status().isNotFound())
               .andExpect(status().reason("Card not found"));
    }

    @Test
    public void testIssueCard_InvalidCardType() throws Exception {
        // Arrange
        String userId = "test-user-id";
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("cardType", "INVALID_TYPE");

        // Act & Assert
        mockMvc.perform(post("/api/loyalty-cards/user/{userId}", userId)
               .contentType(MediaType.APPLICATION_JSON)
               .content(objectMapper.writeValueAsString(requestBody)))
               .andExpect(status().isBadRequest())
               .andExpect(status().reason("Invalid card type"));
    }
}