package ro.unibuc.hello.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ro.unibuc.hello.data.loyalty.LoyaltyCardEntity;
import ro.unibuc.hello.data.loyalty.LoyaltyCardRepository;
import ro.unibuc.hello.data.user.User;
import ro.unibuc.hello.data.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class LoyaltyCardService {
    private static final Logger logger = LoggerFactory.getLogger(LoyaltyCardService.class);

    @Autowired
    private LoyaltyCardRepository loyaltyCardRepository;
    
    @Autowired
    private UserRepository userRepository;

    public LoyaltyCardEntity issueCard(String userId, LoyaltyCardEntity.CardType cardType) throws Exception {
        Optional<User> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            throw new Exception(HttpStatus.NOT_FOUND.toString());
        }
        
        User user = userOpt.get();
        LoyaltyCardEntity loyaltyCard = new LoyaltyCardEntity(cardType, userId);
        LoyaltyCardEntity savedCard = loyaltyCardRepository.save(loyaltyCard);
        
        user.addLoyaltyCardId(savedCard.getId());
        userRepository.save(user);
        
        return savedCard;
    }
    
    public LoyaltyCardEntity getCardById(String id) throws Exception {
        return loyaltyCardRepository.findById(id)
                .orElseThrow(() -> new Exception(HttpStatus.NOT_FOUND.toString()));
    }
    
    public List<LoyaltyCardEntity> getCardsByUser(String userId) throws Exception {
        if (!userRepository.existsById(userId)) {
            throw new Exception(HttpStatus.NOT_FOUND.toString());
        }
        return loyaltyCardRepository.findByUserId(userId);
    }
    
    public LoyaltyCardEntity upgradeCard(String cardId, LoyaltyCardEntity.CardType newType) throws Exception {
        LoyaltyCardEntity card = loyaltyCardRepository.findById(cardId)
                .orElseThrow(() -> new Exception(HttpStatus.NOT_FOUND.toString()));
        
        card.setCardType(newType);
        switch (newType) {
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
        card.setExpiryDate(LocalDate.now().plusYears(2));
        
        return loyaltyCardRepository.save(card);
    }
    
    public LoyaltyCardEntity addPoints(String cardId, int points) throws Exception {
        LoyaltyCardEntity card = loyaltyCardRepository.findById(cardId)
                .orElseThrow(() -> new Exception(HttpStatus.NOT_FOUND.toString()));
        
        card.addPoints(points);
        return loyaltyCardRepository.save(card);
    }
    
    public double calculateDiscount(String cardId, double amount) throws Exception {
        LoyaltyCardEntity card = loyaltyCardRepository.findById(cardId)
                .orElseThrow(() -> new Exception(HttpStatus.NOT_FOUND.toString()));
        
        return amount * (card.getDiscountPercentage() / 100.0);
    }
    
    public void deleteCard(String cardId) throws Exception {
        if (!loyaltyCardRepository.existsById(cardId)) {
            throw new Exception(HttpStatus.NOT_FOUND.toString());
        }
        
        LoyaltyCardEntity card = loyaltyCardRepository.findById(cardId).get();
        String userId = card.getUserId();
        
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.getLoyaltyCardIds().remove(cardId);
            userRepository.save(user);
        }
        
        loyaltyCardRepository.deleteById(cardId);
    }

    @Scheduled(cron = "0 0 0 * * ?") // Rulează zilnic la miezul nopții
    public void checkExpiringCards() {
        List<LoyaltyCardEntity> cards = loyaltyCardRepository.findAll();
        LocalDate today = LocalDate.now();
        for (LoyaltyCardEntity card : cards) {
            if (card.getExpiryDate().isBefore(today.plusDays(30))) {
                logger.warn("Alert: Loyalty card {} (User ID: {}) is expiring soon on {}", 
                    card.getId(), card.getUserId(), card.getExpiryDate());
                // Poate fi extins pentru notificări către utilizator
            }
        }
    }
}