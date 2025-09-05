package ma.tahasouhailmanna.module1.service;

import ma.tahasouhailmanna.module1.dto.ProductDTO;
import ma.tahasouhailmanna.module1.mapper.ProductMapper;
import ma.tahasouhailmanna.module1.model.Product;
import ma.tahasouhailmanna.module1.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public ProductService(ProductRepository productRepository, ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    @Transactional(readOnly = true)
    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(productMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<ProductDTO> getProductById(Long id) {
        return productRepository.findById(id)
                .map(productMapper::toDTO);
    }

    @Transactional
    public ProductDTO saveProduct(ProductDTO productDTO) {
        // Création: on ignore l'id fourni côté client
        Product product = productMapper.toEntity(productDTO);
        product.setId(null);
        Product savedProduct = productRepository.save(product);
        return productMapper.toDTO(savedProduct);
    }

    @Transactional
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }
}
