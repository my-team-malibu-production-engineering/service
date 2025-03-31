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
import ro.unibuc.hello.data.transaction.TransactionDTO;
import ro.unibuc.hello.data.transaction.TransactionEntity;
import ro.unibuc.hello.data.transaction.TransactionEntry;
import ro.unibuc.hello.service.TransactionService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class TransactionControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private TransactionController transactionController;

    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(transactionController).build();

        // Configurare ObjectMapper pentru a gestiona serializarea/deserializarea LocalDateTime
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    public void testCreateTransaction_Success() throws Exception {
        TransactionDTO transactionDTO = createMockTransactionDTO();
        TransactionEntity createdTransaction = createMockTransactionEntity();

        when(transactionService.createTransaction(any(TransactionDTO.class))).thenReturn(createdTransaction);

        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transactionDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("trans123")))
                .andExpect(jsonPath("$.userId", is("user123")))
                .andExpect(jsonPath("$.finalAmount", is(85.5)));

        verify(transactionService, times(1)).createTransaction(any(TransactionDTO.class));
    }

    @Test
    public void testCreateTransaction_ProductOrUserNotFound() throws Exception {
        TransactionDTO transactionDTO = createMockTransactionDTO();

        when(transactionService.createTransaction(any(TransactionDTO.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transactionDTO)))
                .andExpect(status().isNotFound())
                .andExpect(status().reason("Product or user not found"));

        verify(transactionService, times(1)).createTransaction(any(TransactionDTO.class));
    }

    @Test
    public void testCreateTransaction_InsufficientStock() throws Exception {
        TransactionDTO transactionDTO = createMockTransactionDTO();
        // Setăm o cantitate mare pentru a testa stocul insuficient
        transactionDTO.getProductsList().get(0).setProductQuantity(1000);

        when(transactionService.createTransaction(any(TransactionDTO.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST));

        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transactionDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(status().reason("Insufficient stock"));

        verify(transactionService, times(1)).createTransaction(any(TransactionDTO.class));
    }

    @Test
    public void testCreateTransaction_ServerError() throws Exception {
        TransactionDTO transactionDTO = createMockTransactionDTO();

        when(transactionService.createTransaction(any(TransactionDTO.class)))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transactionDTO)))
                .andExpect(status().isInternalServerError())
                .andExpect(status().reason("Service not available"));

        verify(transactionService, times(1)).createTransaction(any(TransactionDTO.class));
    }

    @Test
    public void testGetTransactionById_Success() throws Exception {
        String id = "trans123";
        TransactionEntity transaction = createMockTransactionEntity();

        when(transactionService.getTransactionById(id)).thenReturn(transaction);

        mockMvc.perform(get("/api/transactions/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(id)))
                .andExpect(jsonPath("$.userId", is("user123")))
                .andExpect(jsonPath("$.totalAmount", is(100.0)))
                .andExpect(jsonPath("$.finalAmount", is(85.5)));

        verify(transactionService, times(1)).getTransactionById(id);
    }

    @Test
    public void testGetTransactionById_NotFound() throws Exception {
        String id = "nonexistent";

        when(transactionService.getTransactionById(id))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

        mockMvc.perform(get("/api/transactions/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(status().reason("Transaction not found"));

        verify(transactionService, times(1)).getTransactionById(id);
    }

    @Test
    public void testGetAllTransactions_Success() throws Exception {
        List<TransactionEntity> transactions = Arrays.asList(
                createMockTransactionEntity(),
                createMockTransactionEntity()
        );
        transactions.get(1).setId("trans456");

        when(transactionService.getAllTransactions()).thenReturn(transactions);

        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is("trans123")))
                .andExpect(jsonPath("$[1].id", is("trans456")));

        verify(transactionService, times(1)).getAllTransactions();
    }

    @Test
    public void testGetAllTransactions_ServerError() throws Exception {
        when(transactionService.getAllTransactions())
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isInternalServerError())
                .andExpect(status().reason("Service not available"));

        verify(transactionService, times(1)).getAllTransactions();
    }

    @Test
    public void testGetTransactionsByUser_Success() throws Exception {
        String userId = "user123";
        List<TransactionEntity> userTransactions = Arrays.asList(
                createMockTransactionEntity(),
                createMockTransactionEntity()
        );
        userTransactions.get(1).setId("trans456");

        when(transactionService.getTransactionsByUser(userId)).thenReturn(userTransactions);

        mockMvc.perform(get("/api/transactions/user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].userId", is(userId)))
                .andExpect(jsonPath("$[1].userId", is(userId)));

        verify(transactionService, times(1)).getTransactionsByUser(userId);
    }

    @Test
    public void testGetTransactionsByUser_UserNotFound() throws Exception {
        String userId = "nonexistent";

        when(transactionService.getTransactionsByUser(userId))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

        mockMvc.perform(get("/api/transactions/user/{userId}", userId))
                .andExpect(status().isNotFound())
                .andExpect(status().reason("User not found"));

        verify(transactionService, times(1)).getTransactionsByUser(userId);
    }

    @Test
    public void testGetTransactionsByDateRange_Success() throws Exception {
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();

        List<TransactionEntity> rangeTransactions = Arrays.asList(
                createMockTransactionEntity(),
                createMockTransactionEntity()
        );
        rangeTransactions.get(1).setId("trans456");

        when(transactionService.getTransactionsByDateRange(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(rangeTransactions);

        String formattedStartDate = startDate.format(DateTimeFormatter.ISO_DATE_TIME);
        String formattedEndDate = endDate.format(DateTimeFormatter.ISO_DATE_TIME);

        mockMvc.perform(get("/api/transactions/range")
                .param("startDate", formattedStartDate)
                .param("endDate", formattedEndDate))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is("trans123")))
                .andExpect(jsonPath("$[1].id", is("trans456")));

        verify(transactionService, times(1)).getTransactionsByDateRange(any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    public void testGetTransactionsByDateRange_ServerError() throws Exception {
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();

        when(transactionService.getTransactionsByDateRange(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenThrow(new RuntimeException("Database error"));

        String formattedStartDate = startDate.format(DateTimeFormatter.ISO_DATE_TIME);
        String formattedEndDate = endDate.format(DateTimeFormatter.ISO_DATE_TIME);

        mockMvc.perform(get("/api/transactions/range")
                .param("startDate", formattedStartDate)
                .param("endDate", formattedEndDate))
                .andExpect(status().isInternalServerError())
                .andExpect(status().reason("Service not available"));

        verify(transactionService, times(1)).getTransactionsByDateRange(any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    public void testDeleteTransaction_Success() throws Exception {
        String id = "trans123";

        doNothing().when(transactionService).deleteTransaction(id);

        mockMvc.perform(delete("/api/transactions/{id}", id))
                .andExpect(status().isNoContent());

        verify(transactionService, times(1)).deleteTransaction(id);
    }

    @Test
    public void testDeleteTransaction_NotFound() throws Exception {
        String id = "nonexistent";

        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND))
                .when(transactionService).deleteTransaction(id);

        mockMvc.perform(delete("/api/transactions/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(status().reason("Transaction not found"));

        verify(transactionService, times(1)).deleteTransaction(id);
    }

    @Test
    public void testDeleteTransaction_ServerError() throws Exception {
        String id = "trans123";

        doThrow(new RuntimeException("Database error"))
                .when(transactionService).deleteTransaction(id);

        mockMvc.perform(delete("/api/transactions/{id}", id))
                .andExpect(status().isInternalServerError())
                .andExpect(status().reason("Service not available"));

        verify(transactionService, times(1)).deleteTransaction(id);
    }

    // Metode helper pentru crearea obiectelor mock pentru teste
    private TransactionDTO createMockTransactionDTO() {
        TransactionDTO dto = new TransactionDTO();
        dto.setUserId("user123");
        
        TransactionEntry entry = new TransactionEntry();
        entry.setProductId("product123");
        entry.setProductQuantity(2);
        
        List<TransactionEntry> products = new ArrayList<>();
        products.add(entry);
        
        dto.setProductsList(products);
        dto.setLoyaltyCardId("card123");
        dto.setUseDiscount(true);
        
        return dto;
    }

    private TransactionEntity createMockTransactionEntity() {
        TransactionEntity entity = new TransactionEntity();
        entity.setId("trans123");
        entity.setUserId("user123");
        
        TransactionEntry entry = new TransactionEntry();
        entry.setProductId("product123");
        entry.setProductQuantity(2);
        
        List<TransactionEntry> products = new ArrayList<>();
        products.add(entry);
        
        entity.setProductsList(products);
        entity.setLoyaltyCardId("card123");
        entity.setUseDiscount(true);
        entity.setDate(LocalDateTime.now());
        entity.setTotalAmount(100.0);
        entity.setDiscountAmount(14.5);
        entity.setPromotionDiscount(5.0);
        entity.setLoyaltyDiscount(9.5);
        entity.setTotalDiscount(14.5);
        entity.setFinalAmount(85.5);
        
        return entity;
    }
}