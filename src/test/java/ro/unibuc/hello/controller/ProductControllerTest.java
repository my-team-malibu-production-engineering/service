package ro.unibuc.hello.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import ro.unibuc.hello.data.product.ProductDTO;
import ro.unibuc.hello.data.product.ProductEntity;
import ro.unibuc.hello.service.ProductService;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class ProductControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(productController).build();
    }

    @Test
    public void testPostProduct_Success() throws Exception {
        // Creăm un produs cosmetic
        ProductDTO productDTO = new ProductDTO(
                "Cremă hidratantă de zi", // name
                89.99f,                   // price
                25,                       // stockSize
                "Îngrijire ten",          // category
                "La Roche-Posay",         // brand
                "Cremă hidratantă cu SPF 30 pentru ten sensibil" // description
        );

        doNothing().when(productService).insertProduct(any(ProductDTO.class));

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productDTO)))
                .andExpect(status().isOk());

        verify(productService, times(1)).insertProduct(any(ProductDTO.class));
    }

    @Test
    public void testPostProduct_BadRequest() throws Exception {
        // Produs cu stoc negativ pentru a testa validarea
        ProductDTO productDTO = new ProductDTO(
                "Șampon reparator", 
                45.50f,         
                -5,              // stoc negativ pentru a testa BAD_REQUEST
                "Îngrijire păr",  
                "L'Oréal Paris",    
                "Șampon pentru păr deteriorat cu ulei de argan"
        );

        doThrow(new RuntimeException(HttpStatus.BAD_REQUEST.toString()))
        .when(productService).insertProduct(any(ProductDTO.class));

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(status().reason("Stock size must be a positive number"));

        verify(productService, times(1)).insertProduct(any(ProductDTO.class));
    }

    @Test
    public void testPostProduct_ServerError() throws Exception {
        ProductDTO productDTO = new ProductDTO(
                "Mascara volume", 
                55.99f,         
                15,             
                "Machiaj",  
                "Maybelline",    
                "Mascara pentru volum extra și lungime"
        );

        doThrow(new RuntimeException("Internal error"))
                .when(productService).insertProduct(any(ProductDTO.class));

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productDTO)))
                .andExpect(status().isInternalServerError())
                .andExpect(status().reason("Service not available"));

        verify(productService, times(1)).insertProduct(any(ProductDTO.class));
    }

    @Test
    public void testGetProductById_Success() throws Exception {
        String id = "1";
        ProductEntity productEntity = new ProductEntity(
                id, 
                "Ruj mat", 
                65.00f, 
                true, 
                10, 
                "Machiaj buze", 
                "MAC Cosmetics", 
                "Ruj mat de lungă durată, culoare intensă"
        );

        when(productService.getProductById(id)).thenReturn(productEntity);

        mockMvc.perform(get("/api/products/{id}", id))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(id)))
                .andExpect(jsonPath("$.name", is("Ruj mat")))
                .andExpect(jsonPath("$.price", is(65.0)))
                .andExpect(jsonPath("$.inStock", is(true)))
                .andExpect(jsonPath("$.stockSize", is(10)))
                .andExpect(jsonPath("$.category", is("Machiaj buze")))
                .andExpect(jsonPath("$.brand", is("MAC Cosmetics")))
                .andExpect(jsonPath("$.description", is("Ruj mat de lungă durată, culoare intensă")));

        verify(productService, times(1)).getProductById(id);
    }

    @Test
    public void testGetProductById_NotFound() throws Exception {
        String id = "999";

        when(productService.getProductById(id)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

        mockMvc.perform(get("/api/products/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(status().reason("Product not found"));

        verify(productService, times(1)).getProductById(id);
    }

    @Test
    public void testGetProductById_ServerError() throws Exception {
        String id = "1";

        when(productService.getProductById(id)).thenThrow(new RuntimeException("Internal error"));

        mockMvc.perform(get("/api/products/{id}", id))
                .andExpect(status().isInternalServerError())
                .andExpect(status().reason("Service not available"));

        verify(productService, times(1)).getProductById(id);
    }

    @Test
    public void testGetAllProducts_Success() throws Exception {
        List<ProductEntity> products = Arrays.asList(
                new ProductEntity("1", "Fond de ten fluid", 120.50f, true, 15, "Machiaj ten", "Estée Lauder", "Fond de ten fluid cu acoperire medie și efect natural"),
                new ProductEntity("2", "Cremă de mâini", 32.99f, true, 30, "Îngrijire corp", "Neutrogena", "Cremă de mâini intensiv hidratantă pentru piele uscată"),
                new ProductEntity("3", "Serum cu vitamina C", 159.99f, false, 0, "Îngrijire ten", "The Ordinary", "Serum antioxidant cu 10% vitamina C pură")
        );

        when(productService.getAllProducts()).thenReturn(products);

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].id", is("1")))
                .andExpect(jsonPath("$[0].name", is("Fond de ten fluid")))
                .andExpect(jsonPath("$[0].brand", is("Estée Lauder")))
                .andExpect(jsonPath("$[1].id", is("2")))
                .andExpect(jsonPath("$[1].name", is("Cremă de mâini")))
                .andExpect(jsonPath("$[2].id", is("3")))
                .andExpect(jsonPath("$[2].name", is("Serum cu vitamina C")))
                .andExpect(jsonPath("$[2].inStock", is(false)));

        verify(productService, times(1)).getAllProducts();
    }

    @Test
    public void testGetAllProducts_ServerError() throws Exception {
        when(productService.getAllProducts()).thenThrow(new RuntimeException("Internal error"));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isInternalServerError())
                .andExpect(status().reason("Service not available"));

        verify(productService, times(1)).getAllProducts();
    }

    @Test
    public void testUpdateProductById_Success() throws Exception {
        String id = "1";
        ProductDTO productDTO = new ProductDTO(
                "Balsam de buze actualizat", 
                29.99f,         
                20,             
                "Îngrijire buze",  
                "Nivea",    
                "Balsam de buze actualizat cu ulei de cocos și SPF 15"
        );

        doNothing().when(productService).updateProductById(eq(id), any(ProductDTO.class));

        mockMvc.perform(put("/api/products/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productDTO)))
                .andExpect(status().isOk());

        verify(productService, times(1)).updateProductById(eq(id), any(ProductDTO.class));
    }

    @Test
    public void testUpdateProductById_NotFound() throws Exception {
        String id = "999";
        ProductDTO productDTO = new ProductDTO(
                "Gel de duș", 
                33.50f,         
                25,             
                "Îngrijire corp",  
                "Dove",    
                "Gel de duș hidratant cu parfum de iasomie"
        );

        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND))
                .when(productService).updateProductById(eq(id), any(ProductDTO.class));

        mockMvc.perform(put("/api/products/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productDTO)))
                .andExpect(status().isNotFound())
                .andExpect(status().reason("Product not found"));

        verify(productService, times(1)).updateProductById(eq(id), any(ProductDTO.class));
    }

    @Test
    public void testUpdateProductById_BadRequest() throws Exception {
        String id = "1";
        ProductDTO productDTO = new ProductDTO(
                "Cremă anti-rid", 
                249.99f,         
                -10,             // Stoc negativ pentru a testa BAD_REQUEST
                "Anti-aging",  
                "Vichy",    
                "Cremă anti-rid de noapte cu acid hialuronic"
        );

        doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST))
                .when(productService).updateProductById(eq(id), any(ProductDTO.class));

        mockMvc.perform(put("/api/products/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(status().reason("Stock size must be a positive number"));

        verify(productService, times(1)).updateProductById(eq(id), any(ProductDTO.class));
    }

    @Test
    public void testDeleteProductById_Success() throws Exception {
        String id = "1";

        doNothing().when(productService).deleteProductById(id);

        mockMvc.perform(delete("/api/product/{id}", id))
                .andExpect(status().isOk());

        verify(productService, times(1)).deleteProductById(id);
    }

    @Test
    public void testDeleteProductById_NotFound() throws Exception {
        String id = "999";

        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND))
                .when(productService).deleteProductById(id);

        mockMvc.perform(delete("/api/product/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(status().reason("Product not found"));

        verify(productService, times(1)).deleteProductById(id);
    }

    @Test
    public void testDeleteProductById_ServerError() throws Exception {
        String id = "1";

        doThrow(new RuntimeException("Internal error"))
                .when(productService).deleteProductById(id);

        mockMvc.perform(delete("/api/product/{id}", id))
                .andExpect(status().isInternalServerError())
                .andExpect(status().reason("Service not available"));

        verify(productService, times(1)).deleteProductById(id);
    }
}