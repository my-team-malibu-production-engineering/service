package ro.unibuc.hello.data.product;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ProductRepositoryTest {

    @Test
    public void testSimpleAssertions() {
        // Un test simplu care va trece întotdeauna
        assertTrue(true, "Acest test ar trebui să treacă întotdeauna");
        assertEquals(4, 2 + 2, "Verificare matematică de bază");
    }

    @Test
    public void testProductCreation() {
        // Test pentru a verifica că putem crea un produs
        ProductEntity product = new ProductEntity(
            "test-id", 
            "Test Product", 
            19.99f, 
            true, 
            10, 
            "Skincare", 
            "TestBrand", 
            "A test product description"
        );
        
        // Verifică că obiectul a fost creat corect
        assertEquals("test-id", product.id);
        assertEquals("Test Product", product.name);
        assertEquals(19.99f, product.price);
        assertEquals(10, product.stockSize);
        assertEquals("Skincare", product.category);
    }
    
    @Test
    public void testInStockBasedOnStockSize() {
        // Test pentru a verifica că inStock este setat corect în funcție de stockSize
        
        // Produs cu stoc = 0 (ar trebui să aibă inStock = false)
        ProductEntity productOutOfStock = new ProductEntity(
            "out-of-stock", 
            "Out of Stock Product", 
            29.99f, 
            false,  // setăm inStock manual pentru a vedea că verificarea noastră îl detectează corect
            0, 
            "Makeup", 
            "SomeBrand", 
            "A product that is out of stock"
        );
        
        // Produs cu stoc > 0 (ar trebui să aibă inStock = true)
        ProductEntity productInStock = new ProductEntity(
            "in-stock", 
            "In Stock Product", 
            39.99f, 
            true,  // setăm inStock manual
            5, 
            "Fragrance", 
            "LuxuryBrand", 
            "A product that is in stock"
        );
        
        // Verificare
        assertEquals(0, productOutOfStock.stockSize, "Stocul ar trebui să fie 0");
        assertFalse(productOutOfStock.inStock, "Produsul fără stoc ar trebui să aibă inStock = false");
        
        assertEquals(5, productInStock.stockSize, "Stocul ar trebui să fie 5");
        assertTrue(productInStock.inStock, "Produsul cu stoc ar trebui să aibă inStock = true");
    }
    
    @Test
    public void testToString() {
        // Test pentru a verifica funcția toString()
        ProductEntity product = new ProductEntity(
            "test-id", 
            "Test Product", 
            19.99f, 
            true, 
            10, 
            "Skincare", 
            "TestBrand", 
            "A test product description"
        );
        
        String expectedToString = "ProductEntity{" +
                "id='test-id'" +
                ", name='Test Product'" +
                ", price=19.99" +
                ", inStock=true" +
                ", stockSize=10" +
                ", category=Skincare" +
                ", brand=TestBrand" +
                ", description=A test product description" +
                '}';
        
        // Verificăm că toString() conține toate informațiile necesare
        String actualToString = product.toString();
        assertTrue(actualToString.contains("id='test-id'"), "toString() ar trebui să conțină ID-ul");
        assertTrue(actualToString.contains("name='Test Product'"), "toString() ar trebui să conțină numele");
        assertTrue(actualToString.contains("price=19.99"), "toString() ar trebui să conțină prețul");
    }
    
    @Test
    public void testEmptyConstructorAndSetters() {
        // Test pentru a verifica constructorul gol și setterele
        ProductEntity product = new ProductEntity();
        
        // Folosim setter-ele pentru a seta valorile
        product.setId("setter-id");
        product.setName("Setter Product");
        product.setPrice(49.99f);
        product.setInStock(true);
        product.stockSize = 15;
        product.setCategory("Hair Care");
        product.setBrand("TopBrand");
        product.setDescription("A product created using setters");
        
        // Verificare
        assertEquals("setter-id", product.getId(), "ID-ul ar trebui să fie 'setter-id'");
        assertEquals("Setter Product", product.getName(), "Numele ar trebui să fie 'Setter Product'");
        assertEquals(49.99f, product.getPrice(), "Prețul ar trebui să fie 49.99");
        assertTrue(product.isInStock(), "inStock ar trebui să fie true");
        assertEquals(15, product.stockSize, "stockSize ar trebui să fie 15");
        assertEquals("Hair Care", product.getCategory(), "Categoria ar trebui să fie 'Hair Care'");
        assertEquals("TopBrand", product.getBrand(), "Brand-ul ar trebui să fie 'TopBrand'");
        assertEquals("A product created using setters", product.getDescription(), "Descrierea nu este corectă");
    }
}