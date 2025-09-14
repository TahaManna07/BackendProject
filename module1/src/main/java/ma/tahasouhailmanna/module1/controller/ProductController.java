package ma.tahasouhailmanna.module1.controller;


import ma.tahasouhailmanna.module1.dto.CsvProcessResult;
import ma.tahasouhailmanna.module1.dto.FileUploadResponse;
import ma.tahasouhailmanna.module1.dto.ProductDTO;
import ma.tahasouhailmanna.module1.service.CsvProcessingService;
import ma.tahasouhailmanna.module1.service.ProductService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.io.ByteArrayInputStream;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService productService;
    private final CsvProcessingService csvProcessingService;
    private final ma.tahasouhailmanna.module1.client.Module2Client module2Client;


    public ProductController(ProductService productService, CsvProcessingService csvProcessingService,
                             ma.tahasouhailmanna.module1.client.Module2Client module2Client) {
        this.productService = productService;
        this.csvProcessingService = csvProcessingService;
        this.module2Client = module2Client;
    }
    @GetMapping
    public List<ProductDTO> getAllProducts() {
        return productService.getAllProducts();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        return productService.getProductById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
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
    @PostMapping(value = "/upload-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileUploadResponse> uploadAndStore(@RequestPart("file") MultipartFile file) throws Exception {
        FileUploadResponse resp = csvProcessingService.storeCsv(file);
        return ResponseEntity.ok(resp);
    }

    @PostMapping(value = "/process-csv/external", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<InputStreamResource> processCsvViaModule2(@RequestPart("file") MultipartFile file) throws Exception {
        byte[] processed = module2Client.processCsv(file);
        String outName = "processed_" + (file.getOriginalFilename() != null ? file.getOriginalFilename() : "result.csv");
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header("Content-Disposition", "attachment; filename=\"" + outName + "\"")
                .body(new InputStreamResource(new ByteArrayInputStream(processed)));
    }
}
