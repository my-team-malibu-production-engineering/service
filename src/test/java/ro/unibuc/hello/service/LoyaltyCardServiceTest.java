package ro.unibuc.hello.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ro.unibuc.hello.data.loyalty.LoyaltyCardEntity;
import ro.unibuc.hello.data.loyalty.LoyaltyCardRepository;
import ro.unibuc.hello.data.user.User;
import ro.unibuc.hello.data.user.UserRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoyaltyCardServiceTest {

    @Mock
    private LoyaltyCardRepository loyaltyCardRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private LoyaltyCardService loyaltyCardService;

    private User testUser;
    private LoyaltyCardEntity testCard;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId("user1");
        testUser.setLoyaltyCardIds(new ArrayList<>());

        testCard = new LoyaltyCardEntity(LoyaltyCardEntity.CardType.BRONZE, "user1");
        testCard.setId("card1");
        testCard.setDiscountPercentage(5.0);
    }

    @Test
    void issueCard_UserExists_ReturnsSavedCard() throws Exception {
        when(userRepository.findById("user1")).thenReturn(Optional.of(testUser));
        when(loyaltyCardRepository.save(any(LoyaltyCardEntity.class))).thenReturn(testCard);

        LoyaltyCardEntity result = loyaltyCardService.issueCard("user1", LoyaltyCardEntity.CardType.BRONZE);

        assertNotNull(result);
        assertEquals("card1", result.getId());
        assertEquals("user1", result.getUserId());
        assertEquals(LoyaltyCardEntity.CardType.BRONZE, result.getCardType());
        verify(userRepository).save(testUser);
        assertTrue(testUser.getLoyaltyCardIds().contains("card1"));
    }

    @Test
    void issueCard_UserNotFound_ThrowsException() {
        when(userRepository.findById("user1")).thenReturn(Optional.empty());

        Exception exception = assertThrows(Exception.class, () ->
                loyaltyCardService.issueCard("user1", LoyaltyCardEntity.CardType.BRONZE));
        assertEquals("404 NOT_FOUND", exception.getMessage());
    }

    @Test
    void getCardById_CardExists_ReturnsCard() throws Exception {
        when(loyaltyCardRepository.findById("card1")).thenReturn(Optional.of(testCard));

        LoyaltyCardEntity result = loyaltyCardService.getCardById("card1");

        assertNotNull(result);
        assertEquals("card1", result.getId());
    }

    @Test
    void getCardById_CardNotFound_ThrowsException() {
        when(loyaltyCardRepository.findById("card1")).thenReturn(Optional.empty());

        Exception exception = assertThrows(Exception.class, () ->
                loyaltyCardService.getCardById("card1"));
        assertEquals("404 NOT_FOUND", exception.getMessage());
    }

    @Test
    void getCardsByUser_UserExists_ReturnsCardList() throws Exception {
        when(userRepository.existsById("user1")).thenReturn(true);
        when(loyaltyCardRepository.findByUserId("user1")).thenReturn(List.of(testCard));

        List<LoyaltyCardEntity> result = loyaltyCardService.getCardsByUser("user1");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testCard, result.get(0));
    }

    @Test
    void getCardsByUser_UserNotFound_ThrowsException() {
        when(userRepository.existsById("user1")).thenReturn(false);

        Exception exception = assertThrows(Exception.class, () ->
                loyaltyCardService.getCardsByUser("user1"));
        assertEquals("404 NOT_FOUND", exception.getMessage());
    }

    @Test
    void upgradeCard_CardExists_UpdatesCard() throws Exception {
        when(loyaltyCardRepository.findById("card1")).thenReturn(Optional.of(testCard));
        when(loyaltyCardRepository.save(any(LoyaltyCardEntity.class))).thenReturn(testCard);

        LoyaltyCardEntity result = loyaltyCardService.upgradeCard("card1", LoyaltyCardEntity.CardType.GOLD);

        assertEquals(LoyaltyCardEntity.CardType.GOLD, result.getCardType());
        assertEquals(10.0, result.getDiscountPercentage());
        assertEquals(LocalDate.now().plusYears(2), result.getExpiryDate());
    }

    @Test
    void addPoints_CardExists_AddsPoints() throws Exception {
        testCard.setPoints(100);
        when(loyaltyCardRepository.findById("card1")).thenReturn(Optional.of(testCard));
        when(loyaltyCardRepository.save(any(LoyaltyCardEntity.class))).thenReturn(testCard);

        LoyaltyCardEntity result = loyaltyCardService.addPoints("card1", 50);

        assertEquals(150, result.getPoints());
    }

    @Test
    void calculateDiscount_CardExists_ReturnsDiscount() throws Exception {
        when(loyaltyCardRepository.findById("card1")).thenReturn(Optional.of(testCard));

        double discount = loyaltyCardService.calculateDiscount("card1", 100.0);

        assertEquals(5.0, discount); // 5% of 100
    }

    @Test
    void deleteCard_CardExists_DeletesCardAndUpdatesUser() throws Exception {
        when(loyaltyCardRepository.existsById("card1")).thenReturn(true);
        when(loyaltyCardRepository.findById("card1")).thenReturn(Optional.of(testCard));
        when(userRepository.findById("user1")).thenReturn(Optional.of(testUser));

        testUser.getLoyaltyCardIds().add("card1");
        loyaltyCardService.deleteCard("card1");

        verify(loyaltyCardRepository).deleteById("card1");
        verify(userRepository).save(testUser);
        assertFalse(testUser.getLoyaltyCardIds().contains("card1"));
    }

    @Test
    void deleteCard_CardNotFound_ThrowsException() {
        when(loyaltyCardRepository.existsById("card1")).thenReturn(false);

        Exception exception = assertThrows(Exception.class, () ->
                loyaltyCardService.deleteCard("card1"));
        assertEquals("404 NOT_FOUND", exception.getMessage());
    }
}