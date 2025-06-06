package ro.unibuc.hello.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ro.unibuc.hello.data.product.ProductDTO;
import ro.unibuc.hello.data.product.ProductEntity;
import ro.unibuc.hello.data.product.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

@Service
public class ProductService {
    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    @Autowired
    private ProductRepository productRepository;

    public void insertProduct(ProductDTO product) throws Exception {
        ProductEntity productToSave = new ProductEntity();
        productToSave.id = UUID.randomUUID().toString();
        productToSave.name = product.name;
        productToSave.price = product.price;
        productToSave.stockSize = product.stockSize;
        productToSave.category = product.category;
        productToSave.brand = product.brand;
        productToSave.description = product.description;

        if (productToSave.stockSize < 0) {
            throw new Exception(HttpStatus.BAD_REQUEST.toString());
        }
        productToSave.inStock = productToSave.stockSize > 0;

        productRepository.save(productToSave);
        checkLowStock(productToSave);
    }

    public ProductEntity getProductById(String id) throws Exception {
        return productRepository.findById(id)
                .orElseThrow(() -> new Exception(HttpStatus.NOT_FOUND.toString()));
    }

    public List<ProductEntity> getAllProducts() {
        return productRepository.findAll();
    }

    public void updateProductById(String id, ProductDTO updateProduct) throws Exception {
        ProductEntity productInDb = getProductById(id);
        productInDb.name = updateProduct.name;
        productInDb.price = updateProduct.price;
        productInDb.stockSize = updateProduct.stockSize;
        productInDb.category = updateProduct.category;
        productInDb.brand = updateProduct.brand;
        productInDb.description = updateProduct.description;

        if (productInDb.stockSize < 0) {
            throw new Exception(HttpStatus.BAD_REQUEST.toString());
        }
        productInDb.inStock = productInDb.stockSize > 0;

        productRepository.save(productInDb);
        checkLowStock(productInDb);
    }

    public void deleteProductById(String id) {
        productRepository.deleteById(id);
    }

    private void checkLowStock(ProductEntity product) {
        if (product.stockSize < 5) {
            logger.warn("Alert: Low stock for product {} (ID: {}), stockSize: {}", 
                product.name, product.id, product.stockSize);
            // Poate fi extins pentru notificări externe (ex. email)
        }
    }
}