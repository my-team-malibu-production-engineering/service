package ro.unibuc.hello.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ro.unibuc.hello.data.loyalty.LoyaltyCardEntity;
import ro.unibuc.hello.data.loyalty.LoyaltyCardRepository;
import ro.unibuc.hello.data.product.ProductEntity;
import ro.unibuc.hello.data.product.ProductRepository;
import ro.unibuc.hello.data.promotions.PromotionEntity;
import ro.unibuc.hello.data.promotions.PromotionRepository;
import ro.unibuc.hello.data.transaction.TransactionDTO;
import ro.unibuc.hello.data.transaction.TransactionEntity;
import ro.unibuc.hello.data.transaction.TransactionEntry;
import ro.unibuc.hello.data.transaction.TransactionRepository;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserService userService;

    @Mock
    private LoyaltyCardService loyaltyCardService;

    @Mock
    private LoyaltyCardRepository loyaltyCardRepository;

    @Mock
    private PromotionRepository promotionRepository;

    @InjectMocks
    private TransactionService transactionService;

    private TransactionDTO transactionDTO;
    private TransactionEntity transactionEntity;
    private ProductEntity productEntity;
    private TransactionEntry transactionEntry;
    private LoyaltyCardEntity loyaltyCardEntity;
    private PromotionEntity promotionEntity;

    @BeforeEach
    void setUp() {
        // Setup ProductEntity
        productEntity = new ProductEntity();
        productEntity.id = "prod1";
        productEntity.price = 10.0f;
        productEntity.stockSize = 10;
        productEntity.category = "Category1";

        // Setup TransactionEntry
        transactionEntry = new TransactionEntry();
        transactionEntry.setProductId("prod1");
        transactionEntry.setProductQuantity(2);

        // Setup TransactionDTO
        transactionDTO = new TransactionDTO();
        transactionDTO.setUserId("user1");
        transactionDTO.setProductsList(List.of(transactionEntry));
        transactionDTO.setLoyaltyCardId("card1");
        transactionDTO.setUseDiscount(true);

        // Setup TransactionEntity
        transactionEntity = new TransactionEntity();
        transactionEntity.setId("trans1");
        transactionEntity.setUserId("user1");
        transactionEntity.setProductsList(List.of(transactionEntry));
        transactionEntity.setLoyaltyCardId("card1");
        transactionEntity.setUseDiscount(true);
        transactionEntity.setDate(LocalDateTime.now());
        transactionEntity.setTotalAmount(20.0);
        transactionEntity.setPromotionDiscount(0.0);
        transactionEntity.setLoyaltyDiscount(0.0);
        transactionEntity.setTotalDiscount(0.0);
        transactionEntity.setFinalAmount(20.0);

        // Setup LoyaltyCardEntity
        loyaltyCardEntity = new LoyaltyCardEntity();
        loyaltyCardEntity.setId("card1");
        loyaltyCardEntity.setPoints(0);

        // Setup PromotionEntity
        promotionEntity = new PromotionEntity();
        promotionEntity.setId("promo1");
        promotionEntity.setType(PromotionEntity.PromotionType.BUY_X_GET_Y_FREE);
        promotionEntity.setBuyQuantity(2);
        promotionEntity.setFreeQuantity(1);
        Set<String> categories = new HashSet<>();
        categories.add("Category1");
        promotionEntity.setApplicableCategories(new String[]{"Category1"});
        promotionEntity.setActive(true);
    }

    @Test
    void createTransaction_ValidTransaction_SavesTransaction() throws Exception {
        when(userService.getUserById("user1")).thenReturn(null); // Assuming user exists
        when(productRepository.findById("prod1")).thenReturn(Optional.of(productEntity));
        when(promotionRepository.findByActiveTrue()).thenReturn(List.of(promotionEntity));
        when(loyaltyCardService.calculateDiscount("card1", 20.0)).thenReturn(2.0);
        when(transactionRepository.save(any(TransactionEntity.class))).thenReturn(transactionEntity);

        TransactionEntity result = transactionService.createTransaction(transactionDTO);

        assertEquals(transactionEntity, result);
        assertEquals(8, productEntity.stockSize); // Stock reduced by 2
        verify(productRepository).save(productEntity);
        verify(loyaltyCardService).addPoints(eq("card1"), anyInt());
        verify(transactionRepository).save(any(TransactionEntity.class));
    }

    @Test
    void createTransaction_UserNotFound_ThrowsException() throws Exception {
        when(userService.getUserById("user1")).thenThrow(new Exception("404 NOT_FOUND"));

        Exception exception = assertThrows(Exception.class, () -> {
            transactionService.createTransaction(transactionDTO);
        });
        
        assertEquals("404 NOT_FOUND", exception.getMessage());
    }

    @Test
    void createTransaction_ProductNotFound_ThrowsNotFound() throws Exception {
        when(userService.getUserById("user1")).thenReturn(null);
        when(productRepository.findById("prod1")).thenReturn(Optional.empty());

        Exception exception = assertThrows(Exception.class, () -> {
            transactionService.createTransaction(transactionDTO);
        });
        
        assertEquals("404 NOT_FOUND", exception.getMessage());
    }

    @Test
    void createTransaction_InsufficientStock_ThrowsBadRequest() throws Exception {
        productEntity.stockSize = 1;
        when(userService.getUserById("user1")).thenReturn(null);
        when(productRepository.findById("prod1")).thenReturn(Optional.of(productEntity));

        Exception exception = assertThrows(Exception.class, () -> {
            transactionService.createTransaction(transactionDTO);
        });
        
        assertEquals("400 BAD_REQUEST", exception.getMessage());
    }

    @Test
    void getTransactionById_TransactionExists_ReturnsTransaction() throws Exception {
        when(transactionRepository.findById("trans1")).thenReturn(Optional.of(transactionEntity));

        TransactionEntity result = transactionService.getTransactionById("trans1");

        assertEquals(transactionEntity, result);
    }

    @Test
    void getTransactionById_TransactionNotFound_ThrowsNotFound() throws Exception {
        when(transactionRepository.findById("trans1")).thenReturn(Optional.empty());

        Exception exception = assertThrows(Exception.class, () -> {
            transactionService.getTransactionById("trans1");
        });
        
        assertEquals("404 NOT_FOUND", exception.getMessage());
    }

    @Test
    void getAllTransactions_ReturnsAllTransactions() {
        when(transactionRepository.findAll()).thenReturn(Collections.singletonList(transactionEntity));

        List<TransactionEntity> result = transactionService.getAllTransactions();

        assertEquals(1, result.size());
        assertEquals(transactionEntity, result.get(0));
    }

    @Test
    void getTransactionsByUser_UserExists_ReturnsTransactions() throws Exception {
        when(userService.getUserById("user1")).thenReturn(null);
        when(transactionRepository.findByUserId("user1")).thenReturn(Collections.singletonList(transactionEntity));

        List<TransactionEntity> result = transactionService.getTransactionsByUser("user1");

        assertEquals(1, result.size());
        assertEquals(transactionEntity, result.get(0));
    }

    @Test
    void getTransactionsByUser_UserNotFound_ThrowsException() throws Exception {
        when(userService.getUserById("user1")).thenThrow(new Exception("404 NOT_FOUND"));

        Exception exception = assertThrows(Exception.class, () -> {
            transactionService.getTransactionsByUser("user1");
        });
        
        assertEquals("404 NOT_FOUND", exception.getMessage());
    }

    @Test
    void getTransactionsByDateRange_ReturnsTransactions() {
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(1);
        when(transactionRepository.findByDateBetween(startDate, endDate))
                .thenReturn(Collections.singletonList(transactionEntity));

        List<TransactionEntity> result = transactionService.getTransactionsByDateRange(startDate, endDate);

        assertEquals(1, result.size());
        assertEquals(transactionEntity, result.get(0));
    }

    @Test
    void deleteTransaction_TransactionExists_DeletesTransaction() throws Exception {
        when(transactionRepository.findById("trans1")).thenReturn(Optional.of(transactionEntity));
        when(productRepository.findById("prod1")).thenReturn(Optional.of(productEntity));
        when(loyaltyCardRepository.findById("card1")).thenReturn(Optional.of(loyaltyCardEntity));

        transactionService.deleteTransaction("trans1");

        assertEquals(12, productEntity.stockSize); // Stock increased by 2
        verify(productRepository).save(productEntity);
        verify(loyaltyCardRepository).save(loyaltyCardEntity);
        verify(transactionRepository).deleteById("trans1");
    }

    @Test
    void deleteTransaction_TransactionNotFound_ThrowsNotFound() throws Exception {
        when(transactionRepository.findById("trans1")).thenReturn(Optional.empty());

        Exception exception = assertThrows(Exception.class, () -> {
            transactionService.deleteTransaction("trans1");
        });
        
        assertEquals("404 NOT_FOUND", exception.getMessage());
    }
}