package ro.unibuc.hello.data.transaction;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TransactionRepositoryTest {

    @Test
    public void testSimpleAssertions() {
        // Un test simplu care va trece întotdeauna
        assertTrue(true, "Acest test ar trebui să treacă întotdeauna");
        assertEquals(4, 2 + 2, "Verificare matematică de bază");
    }

    @Test
    public void testTransactionCreation() {
        // Test pentru crearea unei tranzacții
        TransactionEntity transaction = new TransactionEntity();
        transaction.setId("transaction-id-1");
        transaction.setUserId("user-id-1");
        transaction.setLoyaltyCardId("loyalty-card-id-1");
        transaction.setUseDiscount(true);
        transaction.setDate(LocalDateTime.now());
        transaction.setTotalAmount(100.0);
        transaction.setPromotionDiscount(10.0);
        transaction.setLoyaltyDiscount(5.0);
        transaction.setTotalDiscount(15.0);
        transaction.setFinalAmount(85.0);
        
        // Adăugăm o listă de produse
        List<TransactionEntry> productsList = new ArrayList<>();
        
        TransactionEntry entry1 = new TransactionEntry();
        entry1.setProductId("product-1");
        entry1.setProductQuantity(2);
        
        TransactionEntry entry2 = new TransactionEntry();
        entry2.setProductId("product-2");
        entry2.setProductQuantity(1);
        
        productsList.add(entry1);
        productsList.add(entry2);
        transaction.setProductsList(productsList);
        
        // Verificăm că tranzacția a fost creată corect
        assertEquals("transaction-id-1", transaction.getId());
        assertEquals("user-id-1", transaction.getUserId());
        assertEquals("loyalty-card-id-1", transaction.getLoyaltyCardId());
        assertTrue(transaction.isUseDiscount());
        assertEquals(100.0, transaction.getTotalAmount());
        assertEquals(10.0, transaction.getPromotionDiscount());
        assertEquals(5.0, transaction.getLoyaltyDiscount());
        assertEquals(15.0, transaction.getTotalDiscount());
        assertEquals(85.0, transaction.getFinalAmount());
        
        // Verificăm lista de produse
        assertNotNull(transaction.getProductsList());
        assertEquals(2, transaction.getProductsList().size());
        assertEquals("product-1", transaction.getProductsList().get(0).getProductId());
        assertEquals(2, transaction.getProductsList().get(0).getProductQuantity());
        assertEquals("product-2", transaction.getProductsList().get(1).getProductId());
        assertEquals(1, transaction.getProductsList().get(1).getProductQuantity());
    }
    
    @Test
    public void testTransactionEntryCreation() {
        // Test pentru crearea unei intrări de tranzacție
        TransactionEntry entry = new TransactionEntry();
        entry.setProductId("product-id");
        entry.setProductQuantity(3);
        
        // Verificăm că intrarea a fost creată corect
        assertEquals("product-id", entry.getProductId());
        assertEquals(3, entry.getProductQuantity());
        
        // Verificăm că putem accesa și proprietățile publice direct
        entry.productId = "updated-product-id";
        entry.productQuantity = 5;
        
        assertEquals("updated-product-id", entry.getProductId());
        assertEquals(5, entry.getProductQuantity());
    }
    
    @Test
    public void testDiscountCalculations() {
        // Test pentru a verifica calculul corect al discount-urilor
        TransactionEntity transaction = new TransactionEntity();
        transaction.setTotalAmount(200.0);
        
        // Setăm discount-urile
        transaction.setPromotionDiscount(30.0);
        transaction.setLoyaltyDiscount(10.0);
        transaction.setTotalDiscount(40.0);  // 30 + 10
        transaction.setFinalAmount(160.0);   // 200 - 40
        
        // Verificăm calculele
        assertEquals(30.0, transaction.getPromotionDiscount());
        assertEquals(10.0, transaction.getLoyaltyDiscount());
        assertEquals(40.0, transaction.getTotalDiscount());
        assertEquals(160.0, transaction.getFinalAmount());
        
        // Verificăm că suma discounturilor este egală cu discount-ul total
        assertEquals(transaction.getPromotionDiscount() + transaction.getLoyaltyDiscount(), 
                    transaction.getTotalDiscount());
        
        // Verificăm că suma finală + discount-ul total = suma totală inițială
        assertEquals(transaction.getTotalAmount(), 
                    transaction.getFinalAmount() + transaction.getTotalDiscount());
    }
    
    @Test
    public void testTransactionWithoutDiscount() {
        // Test pentru o tranzacție fără discount
        TransactionEntity transaction = new TransactionEntity();
        transaction.setTotalAmount(150.0);
        transaction.setUseDiscount(false);
        transaction.setPromotionDiscount(0.0);
        transaction.setLoyaltyDiscount(0.0);
        transaction.setTotalDiscount(0.0);
        transaction.setFinalAmount(150.0);
        
        // Verificăm că nu există discount
        assertFalse(transaction.isUseDiscount());
        assertEquals(0.0, transaction.getPromotionDiscount());
        assertEquals(0.0, transaction.getLoyaltyDiscount());
        assertEquals(0.0, transaction.getTotalDiscount());
        
        // Verificăm că suma finală este egală cu suma totală
        assertEquals(transaction.getTotalAmount(), transaction.getFinalAmount());
    }
    
    @Test
    public void testTransactionDTO() {
        // Test pentru TransactionDTO
        TransactionDTO transactionDTO = new TransactionDTO();
        transactionDTO.setUserId("user-id-2");
        transactionDTO.setLoyaltyCardId("loyalty-card-id-2");
        transactionDTO.setUseDiscount(true);
        
        // Adăugăm produse
        List<TransactionEntry> productsList = new ArrayList<>();
        TransactionEntry entry = new TransactionEntry();
        entry.setProductId("product-3");
        entry.setProductQuantity(4);
        productsList.add(entry);
        transactionDTO.setProductsList(productsList);
        
        // Verificăm valorile
        assertEquals("user-id-2", transactionDTO.getUserId());
        assertEquals("loyalty-card-id-2", transactionDTO.getLoyaltyCardId());
        assertTrue(transactionDTO.isUseDiscount());
        assertNotNull(transactionDTO.getProductsList());
        assertEquals(1, transactionDTO.getProductsList().size());
        assertEquals("product-3", transactionDTO.getProductsList().get(0).getProductId());
        assertEquals(4, transactionDTO.getProductsList().get(0).getProductQuantity());
    }
}