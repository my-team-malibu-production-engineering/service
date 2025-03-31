package ro.unibuc.hello.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ro.unibuc.hello.data.promotions.PromotionEntity;
import ro.unibuc.hello.data.promotions.PromotionRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PromotionServiceTest {

    @Mock
    private PromotionRepository promotionRepository;

    @InjectMocks
    private PromotionService promotionService;

    private PromotionEntity promotionEntity;

    @BeforeEach
    void setUp() {
        promotionEntity = new PromotionEntity();
        promotionEntity.setId("promo1");
        promotionEntity.setName("Test Promotion");
        promotionEntity.setDescription("Test Description");
        promotionEntity.setType(PromotionEntity.PromotionType.BUY_X_GET_Y_FREE);
        promotionEntity.setBuyQuantity(2);
        promotionEntity.setFreeQuantity(1);
        promotionEntity.setDiscountValue(10.0);
        promotionEntity.setStartDate(LocalDateTime.now().minusDays(1));
        promotionEntity.setEndDate(LocalDateTime.now().plusDays(1));
        promotionEntity.setActive(true);
        promotionEntity.setApplicableCategories(new String[]{"Category1"});
    }

    @Test
    void createPromotion_SavesAndReturnsPromotion() {
        when(promotionRepository.save(any(PromotionEntity.class))).thenReturn(promotionEntity);

        PromotionEntity result = promotionService.createPromotion(promotionEntity);

        assertEquals(promotionEntity, result);
        verify(promotionRepository).save(promotionEntity);
    }

    @Test
    void getPromotionById_PromotionExists_ReturnsPromotion() throws Exception {
        when(promotionRepository.findById("promo1")).thenReturn(Optional.of(promotionEntity));

        PromotionEntity result = promotionService.getPromotionById("promo1");

        assertEquals(promotionEntity, result);
    }

    @Test
    void getPromotionById_PromotionNotFound_ThrowsNotFound() {
        when(promotionRepository.findById("promo1")).thenReturn(Optional.empty());

        Exception exception = assertThrows(Exception.class, () ->
                promotionService.getPromotionById("promo1"));
        assertEquals("404 NOT_FOUND", exception.getMessage());
    }

    @Test
    void getAllPromotions_ReturnsAllPromotions() {
        when(promotionRepository.findAll()).thenReturn(Collections.singletonList(promotionEntity));

        List<PromotionEntity> result = promotionService.getAllPromotions();

        assertEquals(1, result.size());
        assertEquals(promotionEntity, result.get(0));
    }

    @Test
    void getActivePromotions_ReturnsActivePromotions() {
        when(promotionRepository.findByActiveTrue()).thenReturn(Collections.singletonList(promotionEntity));

        List<PromotionEntity> result = promotionService.getActivePromotions();

        assertEquals(1, result.size());
        assertEquals(promotionEntity, result.get(0));
    }

    @Test
    void updatePromotion_PromotionExists_UpdatesAndSaves() throws Exception {
        when(promotionRepository.findById("promo1")).thenReturn(Optional.of(promotionEntity));
        when(promotionRepository.save(any(PromotionEntity.class))).thenReturn(promotionEntity);

        PromotionEntity updatedPromotion = new PromotionEntity();
        updatedPromotion.setName("Updated Promotion");
        updatedPromotion.setDescription("Updated Description");
        updatedPromotion.setType(PromotionEntity.PromotionType.PERCENT_DISCOUNT);
        updatedPromotion.setBuyQuantity(1);
        updatedPromotion.setFreeQuantity(0);
        updatedPromotion.setDiscountValue(15.0);
        updatedPromotion.setStartDate(LocalDateTime.now());
        updatedPromotion.setEndDate(LocalDateTime.now().plusDays(2));
        updatedPromotion.setActive(false);
        updatedPromotion.setApplicableCategories(new String[]{"Category2"});

        PromotionEntity result = promotionService.updatePromotion("promo1", updatedPromotion);

        assertEquals(promotionEntity, result);
        verify(promotionRepository).save(argThat(promotion ->
                promotion.getName().equals("Updated Promotion") &&
                promotion.getDescription().equals("Updated Description") &&
                promotion.getType() == PromotionEntity.PromotionType.PERCENT_DISCOUNT &&
                promotion.getBuyQuantity() == 1 &&
                promotion.getFreeQuantity() == 0 &&
                promotion.getDiscountValue() == 15.0 &&
                promotion.getStartDate().equals(updatedPromotion.getStartDate()) &&
                promotion.getEndDate().equals(updatedPromotion.getEndDate()) &&
                !promotion.isActive() &&
                java.util.Arrays.equals(promotion.getApplicableCategories(), new String[]{"Category2"})));
    }

    @Test
    void updatePromotion_PromotionNotFound_ThrowsNotFound() {
        when(promotionRepository.findById("promo1")).thenReturn(Optional.empty());

        PromotionEntity updatedPromotion = new PromotionEntity();

        Exception exception = assertThrows(Exception.class, () ->
                promotionService.updatePromotion("promo1", updatedPromotion));
        assertEquals("404 NOT_FOUND", exception.getMessage());
        verify(promotionRepository, never()).save(any(PromotionEntity.class));
    }

    @Test
    void deletePromotion_PromotionExists_DeletesPromotion() throws Exception {
        when(promotionRepository.existsById("promo1")).thenReturn(true);

        promotionService.deletePromotion("promo1");

        verify(promotionRepository).deleteById("promo1");
    }

    @Test
    void deletePromotion_PromotionNotFound_ThrowsNotFound() {
        when(promotionRepository.existsById("promo1")).thenReturn(false);

        Exception exception = assertThrows(Exception.class, () ->
                promotionService.deletePromotion("promo1"));
        assertEquals("404 NOT_FOUND", exception.getMessage());
        verify(promotionRepository, never()).deleteById(anyString());
    }
}