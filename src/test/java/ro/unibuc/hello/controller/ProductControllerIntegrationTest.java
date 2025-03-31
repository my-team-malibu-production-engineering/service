package ro.unibuc.hello.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ro.unibuc.hello.data.product.ProductDTO;
import ro.unibuc.hello.data.product.ProductEntity;
import ro.unibuc.hello.data.product.ProductRepository;
import ro.unibuc.hello.service.ProductService;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    @MockBean
    private ProductRepository productRepository;

    @Test
    public void testGetProductById() throws Exception {
        // Arrange
        String productId = "test-product-id";
        ProductEntity product = new ProductEntity(
            productId, 
            "Test Product", 
            19.99f, 
            true, 
            10, 
            "Skincare", 
            "TestBrand", 
            "A test product description"
        );
        
        when(productService.getProductById(productId)).thenReturn(product);

        // Act & Assert
        mockMvc.perform(get("/api/products/{id}", productId))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id").value(productId))
               .andExpect(jsonPath("$.name").value("Test Product"))
               .andExpect(jsonPath("$.price").value(19.99))
               .andExpect(jsonPath("$.inStock").value(true))
               .andExpect(jsonPath("$.stockSize").value(10))
               .andExpect(jsonPath("$.category").value("Skincare"))
               .andExpect(jsonPath("$.brand").value("TestBrand"));
    }

    @Test
    public void testGetAllProducts() throws Exception {
        // Arrange
        ProductEntity product1 = new ProductEntity(
            "product-1", 
            "Product 1", 
            19.99f, 
            true, 
            10, 
            "Skincare", 
            "Brand1", 
            "Description 1"
        );
        
        ProductEntity product2 = new ProductEntity(
            "product-2", 
            "Product 2", 
            29.99f, 
            true, 
            5, 
            "Makeup", 
            "Brand2", 
            "Description 2"
        );
        
        List<ProductEntity> products = Arrays.asList(product1, product2);
        
        when(productService.getAllProducts()).thenReturn(products);

        // Act & Assert
        mockMvc.perform(get("/api/products"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$[0].id").value("product-1"))
               .andExpect(jsonPath("$[0].name").value("Product 1"))
               .andExpect(jsonPath("$[1].id").value("product-2"))
               .andExpect(jsonPath("$[1].name").value("Product 2"))
               .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    public void testCreateProduct() throws Exception {
        // Arrange
        ProductDTO productDTO = new ProductDTO(
            "New Product", 
            24.99f, 
            15, 
            "Haircare", 
            "NewBrand", 
            "A new product description"
        );
        
        doNothing().when(productService).insertProduct(any(ProductDTO.class));

        // Act & Assert
        mockMvc.perform(post("/api/products")
               .contentType(MediaType.APPLICATION_JSON)
               .content(objectMapper.writeValueAsString(productDTO)))
               .andExpect(status().isOk());
    }

    @Test
    public void testUpdateProduct() throws Exception {
        // Arrange
        String productId = "product-to-update";
        ProductDTO updateDTO = new ProductDTO(
            "Updated Product", 
            34.99f, 
            20, 
            "Skincare", 
            "UpdatedBrand", 
            "Updated description"
        );
        
        doNothing().when(productService).updateProductById(eq(productId), any(ProductDTO.class));

        // Act & Assert
        mockMvc.perform(put("/api/products/{id}", productId)
               .contentType(MediaType.APPLICATION_JSON)
               .content(objectMapper.writeValueAsString(updateDTO)))
               .andExpect(status().isOk());
    }

    @Test
    public void testDeleteProduct() throws Exception {
        // Arrange
        String productId = "product-to-delete";
        doNothing().when(productService).deleteProductById(productId);

        // Act & Assert
        mockMvc.perform(delete("/api/product/{id}", productId)) // Changed back to singular to match controller endpoint
               .andExpect(status().isOk());
    }

    @Test
    public void testGetProductById_NotFound() throws Exception {
        // Arrange
        String nonExistentId = "non-existent";
        when(productService.getProductById(nonExistentId)).thenThrow(new RuntimeException("404 NOT_FOUND"));

        // Act & Assert
        mockMvc.perform(get("/api/products/{id}", nonExistentId))
               .andExpect(status().isNotFound());
    }
}