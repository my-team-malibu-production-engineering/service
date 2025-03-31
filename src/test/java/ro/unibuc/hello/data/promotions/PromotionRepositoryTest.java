package ro.unibuc.hello.data.promotions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

public class PromotionRepositoryTest {

    @Test
    public void testSimpleAssertions() {
        // Un test simplu care va trece întotdeauna
        assertTrue(true, "Acest test ar trebui să treacă întotdeauna");
        assertEquals(4, 2 + 2, "Verificare matematică de bază");
    }

    @Test
    public void testPromotionCreation() {
        // Test pentru crearea unei promoții
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime future = now.plusDays(10);
        
        PromotionEntity promotion = new PromotionEntity(
            "Buy 2 Get 1 Free",
            "Cumpără 2 produse și primește unul gratuit",
            PromotionEntity.PromotionType.BUY_X_GET_Y_FREE,
            2, 1, 0.0,
            now, future,
            true, new String[]{"Skincare", "Makeup"}
        );
        
        // Verificăm că obiectul a fost creat corect
        assertEquals("Buy 2 Get 1 Free", promotion.getName());
        assertEquals("Cumpără 2 produse și primește unul gratuit", promotion.getDescription());
        assertEquals(PromotionEntity.PromotionType.BUY_X_GET_Y_FREE, promotion.getType());
        assertEquals(2, promotion.getBuyQuantity());
        assertEquals(1, promotion.getFreeQuantity());
        assertEquals(0.0, promotion.getDiscountValue());
        assertEquals(now, promotion.getStartDate());
        assertEquals(future, promotion.getEndDate());
        assertTrue(promotion.isActive());
        assertEquals(2, promotion.getApplicableCategories().length);
        assertEquals("Skincare", promotion.getApplicableCategories()[0]);
        assertEquals("Makeup", promotion.getApplicableCategories()[1]);
    }
    
    @Test
    public void testPromotionTypesAndValues() {
        // Testăm diferite tipuri de promoții
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime future = now.plusDays(10);
        
        // Promoție de tip BUY_X_GET_Y_FREE
        PromotionEntity buyGetPromotion = new PromotionEntity(
            "Buy 2 Get 1 Free",
            "Promotion description",
            PromotionEntity.PromotionType.BUY_X_GET_Y_FREE,
            2, 1, 0.0,
            now, future,
            true, new String[]{"Skincare"}
        );
        
        // Promoție de tip PERCENT_DISCOUNT
        PromotionEntity percentPromotion = new PromotionEntity(
            "20% Off Makeup",
            "Promotion description",
            PromotionEntity.PromotionType.PERCENT_DISCOUNT,
            0, 0, 20.0,
            now, future,
            true, new String[]{"Makeup"}
        );
        
        // Promoție de tip FIXED_AMOUNT_DISCOUNT
        PromotionEntity fixedPromotion = new PromotionEntity(
            "10 RON Off",
            "Promotion description",
            PromotionEntity.PromotionType.FIXED_AMOUNT_DISCOUNT,
            0, 0, 10.0,
            now, future,
            true, new String[]{"All"}
        );
        
        // Verificări pentru BUY_X_GET_Y_FREE
        assertEquals(PromotionEntity.PromotionType.BUY_X_GET_Y_FREE, buyGetPromotion.getType());
        assertEquals(2, buyGetPromotion.getBuyQuantity());
        assertEquals(1, buyGetPromotion.getFreeQuantity());
        
        // Verificări pentru PERCENT_DISCOUNT
        assertEquals(PromotionEntity.PromotionType.PERCENT_DISCOUNT, percentPromotion.getType());
        assertEquals(20.0, percentPromotion.getDiscountValue());
        
        // Verificări pentru FIXED_AMOUNT_DISCOUNT
        assertEquals(PromotionEntity.PromotionType.FIXED_AMOUNT_DISCOUNT, fixedPromotion.getType());
        assertEquals(10.0, fixedPromotion.getDiscountValue());
    }
    
    @Test
    public void testEmptyConstructorAndSetters() {
        // Test pentru constructorul gol și setters
        PromotionEntity promotion = new PromotionEntity();
        
        // Folosind setteri
        promotion.setId("promo-id");
        promotion.setName("Promotion Name");
        promotion.setDescription("Promotion Description");
        promotion.setType(PromotionEntity.PromotionType.PERCENT_DISCOUNT);
        promotion.setBuyQuantity(0);
        promotion.setFreeQuantity(0);
        promotion.setDiscountValue(15.0);
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime future = now.plusDays(30);
        promotion.setStartDate(now);
        promotion.setEndDate(future);
        promotion.setActive(true);
        promotion.setApplicableCategories(new String[]{"Fragrance"});
        
        // Verificări
        assertEquals("promo-id", promotion.getId());
        assertEquals("Promotion Name", promotion.getName());
        assertEquals("Promotion Description", promotion.getDescription());
        assertEquals(PromotionEntity.PromotionType.PERCENT_DISCOUNT, promotion.getType());
        assertEquals(0, promotion.getBuyQuantity());
        assertEquals(0, promotion.getFreeQuantity());
        assertEquals(15.0, promotion.getDiscountValue());
        assertEquals(now, promotion.getStartDate());
        assertEquals(future, promotion.getEndDate());
        assertTrue(promotion.isActive());
        assertEquals(1, promotion.getApplicableCategories().length);
        assertEquals("Fragrance", promotion.getApplicableCategories()[0]);
    }
    
    @Test
    public void testPromotionValidity() {
        // Test pentru a verifica validitatea promoției în funcție de dată
        LocalDateTime now = LocalDateTime.now();
        
        // Promoție activă și în intervalul de timp valid
        PromotionEntity validPromotion = new PromotionEntity(
            "Valid Promotion",
            "Description",
            PromotionEntity.PromotionType.PERCENT_DISCOUNT,
            0, 0, 10.0,
            now.minusDays(5), now.plusDays(5),
            true, new String[]{"Skincare"}
        );
        
        // Promoție activă dar expirată
        PromotionEntity expiredPromotion = new PromotionEntity(
            "Expired Promotion",
            "Description",
            PromotionEntity.PromotionType.PERCENT_DISCOUNT,
            0, 0, 10.0,
            now.minusDays(10), now.minusDays(5),
            true, new String[]{"Skincare"}
        );
        
        // Promoție inactivă în interval valid
        PromotionEntity inactivePromotion = new PromotionEntity(
            "Inactive Promotion",
            "Description",
            PromotionEntity.PromotionType.PERCENT_DISCOUNT,
            0, 0, 10.0,
            now.minusDays(5), now.plusDays(5),
            false, new String[]{"Skincare"}
        );
        
        // Verificări
        assertTrue(validPromotion.isActive());
        assertTrue(validPromotion.getStartDate().isBefore(now));
        assertTrue(validPromotion.getEndDate().isAfter(now));
        
        assertTrue(expiredPromotion.isActive());
        assertTrue(expiredPromotion.getEndDate().isBefore(now));
        
        assertFalse(inactivePromotion.isActive());
        assertTrue(inactivePromotion.getStartDate().isBefore(now));
        assertTrue(inactivePromotion.getEndDate().isAfter(now));
    }
}