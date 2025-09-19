package ma.tahasouhailmanna.module1.controller;


import jakarta.validation.Valid;
import ma.tahasouhailmanna.module1.dto.ProductDTO;
import ma.tahasouhailmanna.module1.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService productService;



    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public List<ProductDTO> getAllProducts() {
        return productService.getAllProducts();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        return productService.getProductById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(@RequestBody ProductDTO productDTO) {
        ProductDTO savedProduct = productService.saveProduct(productDTO);
        return ResponseEntity.ok(savedProduct);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public Page<ProductDTO> search(@Valid ma.tahasouhailmanna.module1.criteria.ProductCriteria criteria, Pageable pageable) {
        return productService.search(criteria, pageable);
    }

    @GetMapping("/search/name")
    public List<ProductDTO> searchByName(@RequestParam String name) {
        return productService.findByName(name);
    }

    @GetMapping("/search/description")
    public List<ProductDTO> searchByDescription(@RequestParam String description) {
        return productService.findByDescription(description);
    }

    @GetMapping("/search/category")
    public List<ProductDTO> searchByCategory(@RequestParam String category) {
        return productService.findByCategory(category);
    }

    @GetMapping("/search/price")
    public List<ProductDTO> searchByPriceBetween(@RequestParam Double min, @RequestParam Double max) {
        return productService.findByPriceBetween(min, max);
    }




}
