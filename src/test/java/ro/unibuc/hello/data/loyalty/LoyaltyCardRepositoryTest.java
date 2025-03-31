package ro.unibuc.hello.data.loyalty;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

public class LoyaltyCardRepositoryTest {

    @Test
    public void testSimpleAssertions() {
        // Un test simplu care va trece întotdeauna
        assertTrue(true, "Acest test ar trebui să treacă întotdeauna");
        assertEquals(4, 2 + 2, "Verificare matematică de bază");
    }

    @Test
    public void testLoyaltyCardCreation() {
        // Test pentru crearea unui card de fidelitate
        LoyaltyCardEntity card = new LoyaltyCardEntity(
            LoyaltyCardEntity.CardType.GOLD,
            "user-id-1"
        );
        
        // Verificăm că obiectul a fost creat corect
        assertEquals(LoyaltyCardEntity.CardType.GOLD, card.getCardType());
        assertEquals("user-id-1", card.getUserId());
        assertEquals(0, card.getPoints()); // Ar trebui să înceapă cu 0 puncte
        assertNotNull(card.getIssueDate()); // Data emiterii ar trebui să fie setată
        assertNotNull(card.getExpiryDate()); // Data expirării ar trebui să fie setată
        
        // Verificăm că data expirării este la 2 ani după data emiterii
        assertEquals(card.getIssueDate().plusYears(2), card.getExpiryDate());
        
        // Verificăm discount-ul pentru cardul GOLD
        assertEquals(10.0, card.getDiscountPercentage());
    }
    
    @Test
    public void testDiscountPercentageByCardType() {
        // Test pentru a verifica procentele de discount în funcție de tipul cardului
        LoyaltyCardEntity bronzeCard = new LoyaltyCardEntity(LoyaltyCardEntity.CardType.BRONZE, "user-1");
        LoyaltyCardEntity goldCard = new LoyaltyCardEntity(LoyaltyCardEntity.CardType.GOLD, "user-1");
        LoyaltyCardEntity premiumCard = new LoyaltyCardEntity(LoyaltyCardEntity.CardType.PREMIUM, "user-1");
        
        // Verificări pentru discount
        assertEquals(5.0, bronzeCard.getDiscountPercentage());
        assertEquals(10.0, goldCard.getDiscountPercentage());
        assertEquals(15.0, premiumCard.getDiscountPercentage());
    }
    
    @Test
    public void testAddPoints() {
        // Test pentru adăugarea de puncte
        LoyaltyCardEntity card = new LoyaltyCardEntity(LoyaltyCardEntity.CardType.BRONZE, "user-id-1");
        assertEquals(0, card.getPoints()); // Inițial 0 puncte
        
        // Adăugăm puncte
        card.addPoints(50);
        assertEquals(50, card.getPoints());
        
        // Adăugăm mai multe puncte
        card.addPoints(25);
        assertEquals(75, card.getPoints());
    }
    
    @Test
    public void testExpiryDateLogic() {
        // Test pentru logica datei de expirare
        LocalDate today = LocalDate.now();
        
        // Creăm un card nou
        LoyaltyCardEntity card = new LoyaltyCardEntity(LoyaltyCardEntity.CardType.BRONZE, "user-id-1");
        
        // Verificăm datele
        assertEquals(today, card.getIssueDate());
        assertEquals(today.plusYears(2), card.getExpiryDate());
        
        // Setăm o nouă dată de expirare
        LocalDate newExpiryDate = today.plusYears(3);
        card.setExpiryDate(newExpiryDate);
        assertEquals(newExpiryDate, card.getExpiryDate());
    }
    
    @Test
    public void testEmptyConstructorAndSetters() {
        // Test pentru constructorul gol și setteri
        LoyaltyCardEntity card = new LoyaltyCardEntity();
        
        // Folosim setterii
        card.setId("card-id-1");
        card.setCardType(LoyaltyCardEntity.CardType.PREMIUM);
        card.setUserId("user-id-2");
        card.setPoints(100);
        card.setDiscountPercentage(20.0);
        
        LocalDate issueDate = LocalDate.now().minusMonths(6);
        LocalDate expiryDate = LocalDate.now().plusYears(1).plusMonths(6);
        card.setIssueDate(issueDate);
        card.setExpiryDate(expiryDate);
        
        // Verificări
        assertEquals("card-id-1", card.getId());
        assertEquals(LoyaltyCardEntity.CardType.PREMIUM, card.getCardType());
        assertEquals("user-id-2", card.getUserId());
        assertEquals(100, card.getPoints());
        assertEquals(20.0, card.getDiscountPercentage());
        assertEquals(issueDate, card.getIssueDate());
        assertEquals(expiryDate, card.getExpiryDate());
    }
    
    @Test
    public void testToString() {
        // Test pentru metoda toString()
        LoyaltyCardEntity card = new LoyaltyCardEntity(LoyaltyCardEntity.CardType.BRONZE, "user-id-1");
        card.setId("card-id-1");
        card.addPoints(50);
        
        String toString = card.toString();
        
        // Verificăm că toString conține informațiile de bază
        assertTrue(toString.contains("card-id-1"));
        assertTrue(toString.contains("BRONZE"));
        assertTrue(toString.contains("50"));
        assertTrue(toString.contains("user-id-1"));
    }
}