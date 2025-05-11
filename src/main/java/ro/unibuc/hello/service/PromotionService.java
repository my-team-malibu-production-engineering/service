package ro.unibuc.hello.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ro.unibuc.hello.data.promotions.PromotionEntity;
import ro.unibuc.hello.data.promotions.PromotionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PromotionService {
    private static final Logger logger = LoggerFactory.getLogger(PromotionService.class);

    @Autowired
    private PromotionRepository promotionRepository;
    
    public PromotionEntity createPromotion(PromotionEntity promotion) {
        return promotionRepository.save(promotion);
    }
    
    public PromotionEntity getPromotionById(String id) throws Exception {
        return promotionRepository.findById(id)
                .orElseThrow(() -> new Exception(HttpStatus.NOT_FOUND.toString()));
    }
    
    public List<PromotionEntity> getAllPromotions() {
        return promotionRepository.findAll();
    }
    
    public List<PromotionEntity> getActivePromotions() {
        return promotionRepository.findByActiveTrue();
    }
    
    public PromotionEntity updatePromotion(String id, PromotionEntity updatedPromotion) throws Exception {
        PromotionEntity promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new Exception(HttpStatus.NOT_FOUND.toString()));
        
        promotion.setName(updatedPromotion.getName());
        promotion.setDescription(updatedPromotion.getDescription());
        promotion.setType(updatedPromotion.getType());
        promotion.setBuyQuantity(updatedPromotion.getBuyQuantity());
        promotion.setFreeQuantity(updatedPromotion.getFreeQuantity());
        promotion.setDiscountValue(updatedPromotion.getDiscountValue());
        promotion.setStartDate(updatedPromotion.getStartDate());
        promotion.setEndDate(updatedPromotion.getEndDate());
        promotion.setActive(updatedPromotion.isActive());
        promotion.setApplicableCategories(updatedPromotion.getApplicableCategories());
        
        return promotionRepository.save(promotion);
    }
    
    public void deletePromotion(String id) throws Exception {
        if (!promotionRepository.existsById(id)) {
            throw new Exception(HttpStatus.NOT_FOUND.toString());
        }
        promotionRepository.deleteById(id);
    }

    @Scheduled(cron = "0 0 0 * * ?") // Rulează zilnic la miezul nopții
    public void checkExpiredPromotions() {
        List<PromotionEntity> activePromotions = promotionRepository.findByActiveTrue();
        LocalDateTime now = LocalDateTime.now();
        for (PromotionEntity promotion : activePromotions) {
            if (promotion.getEndDate().isBefore(now)) {
                logger.warn("Alert: Promotion {} (ID: {}) is active but expired on {}", 
                    promotion.getName(), promotion.getId(), promotion.getEndDate());
                // Opțional: Dezactivăm automat promoția
                promotion.setActive(false);
                promotionRepository.save(promotion);
            }
        }
    }
}