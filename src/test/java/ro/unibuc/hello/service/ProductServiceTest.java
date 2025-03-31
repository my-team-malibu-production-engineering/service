package ro.unibuc.hello.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ro.unibuc.hello.data.product.ProductDTO;
import ro.unibuc.hello.data.product.ProductEntity;
import ro.unibuc.hello.data.product.ProductRepository;

import java.util.Collections;
import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private ProductDTO productDTO;
    private ProductEntity productEntity;

    @BeforeEach
    void setUp() {
        productDTO = new ProductDTO("", 0.0f, 0, "", "", "");
        productDTO.setName("Test Product");
        productDTO.setPrice(10.0f);
        productDTO.setStockSize(5);
        productDTO.setCategory("Test Category");
        productDTO.setBrand("Test Brand");
        productDTO.setDescription("Test Description");

        productEntity = new ProductEntity();
        productEntity.id = "test-id";
        productEntity.name = "Test Product";
        productEntity.price = 10.0f;
        productEntity.stockSize = 5;
        productEntity.category = "Test Category";
        productEntity.brand = "Test Brand";
        productEntity.description = "Test Description";
        productEntity.inStock = true;
    }

    @Test
    void insertProduct_ValidStock_SavesProduct() throws Exception {
        when(productRepository.save(any(ProductEntity.class))).thenReturn(productEntity);

        productService.insertProduct(productDTO);

        verify(productRepository).save(argThat(product ->
            product.name.equals("Test Product") &&
            product.price == 10.0f &&
            product.stockSize == 5 &&
            product.inStock &&
            product.category.equals("Test Category") &&
            product.brand.equals("Test Brand") &&
            product.description.equals("Test Description")));
    }

    @Test
    void insertProduct_NegativeStock_ThrowsBadRequest() {
        productDTO.setStockSize(-1);

        Exception exception = assertThrows(Exception.class, () ->
            productService.insertProduct(productDTO));
        assertEquals("400 BAD_REQUEST", exception.getMessage());
        verify(productRepository, never()).save(any(ProductEntity.class));
    }

    @Test
    void insertProduct_ZeroStock_SetsInStockFalse() throws Exception {
        productDTO.setStockSize(0);
        when(productRepository.save(any(ProductEntity.class))).thenReturn(productEntity);

        productService.insertProduct(productDTO);

        verify(productRepository).save(argThat(product -> !product.inStock));
    }

    @Test
    void getProductById_ProductExists_ReturnsProduct() throws Exception {
        when(productRepository.findById("test-id")).thenReturn(Optional.of(productEntity));

        ProductEntity result = productService.getProductById("test-id");

        assertEquals(productEntity, result);
    }

    @Test
    void getProductById_ProductNotFound_ThrowsNotFound() {
        when(productRepository.findById("test-id")).thenReturn(Optional.empty());

        Exception exception = assertThrows(Exception.class, () ->
            productService.getProductById("test-id"));
        assertEquals("404 NOT_FOUND", exception.getMessage());
    }

    @Test
    void getAllProducts_ReturnsAllProducts() {
        when(productRepository.findAll()).thenReturn(Collections.singletonList(productEntity));

        List<ProductEntity> result = productService.getAllProducts();

        assertEquals(1, result.size());
        assertEquals(productEntity, result.get(0));
    }

    @Test
    void updateProductById_ValidUpdate_SavesUpdatedProduct() throws Exception {
        when(productRepository.findById("test-id")).thenReturn(Optional.of(productEntity));
        when(productRepository.save(any(ProductEntity.class))).thenReturn(productEntity);

        ProductDTO updateDTO = new ProductDTO("", 0.0f, 0, "", "", "");
        updateDTO.setName("Updated Product");
        updateDTO.setPrice(20.0f);
        updateDTO.setStockSize(10);
        updateDTO.setCategory("Updated Category");
        updateDTO.setBrand("Updated Brand");
        updateDTO.setDescription("Updated Description");

        productService.updateProductById("test-id", updateDTO);

        verify(productRepository).save(argThat(product ->
            product.name.equals("Updated Product") &&
            product.price == 20.0f &&
            product.stockSize == 10 &&
            product.inStock &&
            product.category.equals("Updated Category") &&
            product.brand.equals("Updated Brand") &&
            product.description.equals("Updated Description")));
    }

    @Test
    void updateProductById_NegativeStock_ThrowsBadRequest() throws Exception {
        when(productRepository.findById("test-id")).thenReturn(Optional.of(productEntity));

        ProductDTO updateDTO = new ProductDTO("", 0.0f, 0, "", "", "");
        updateDTO.setStockSize(-1);

        Exception exception = assertThrows(Exception.class, () ->
            productService.updateProductById("test-id", updateDTO));
        assertEquals("400 BAD_REQUEST", exception.getMessage());
    }

    @Test
    void updateProductById_ProductNotFound_ThrowsNotFound() {
        when(productRepository.findById("test-id")).thenReturn(Optional.empty());

        Exception exception = assertThrows(Exception.class, () ->
            productService.updateProductById("test-id", productDTO));
        assertEquals("404 NOT_FOUND", exception.getMessage());
    }

    @Test
    void deleteProductById_CallsDelete() {
        productService.deleteProductById("test-id");

        verify(productRepository).deleteById("test-id");
    }
}