package ma.tahasouhailmanna.module1.service;

import ma.tahasouhailmanna.module1.criteria.ProductCriteria;
import ma.tahasouhailmanna.module1.dto.ProductDTO;
import ma.tahasouhailmanna.module1.exception.ResourceNotFoundException;
import ma.tahasouhailmanna.module1.mapper.ProductMapper;
import ma.tahasouhailmanna.module1.model.Product;
import ma.tahasouhailmanna.module1.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;

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
    @Cacheable(value = "products")
    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(productMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "product", key = "#id")
    public Optional<ProductDTO> getProductById(Long id) {
        return productRepository.findById(id)
                .map(productMapper::toDto);
    }

    @Transactional
    @Caching(
        put = @CachePut(value = "product", key = "#result.id"),
        evict = @CacheEvict(value = "products", allEntries = true)
    )
    public ProductDTO saveProduct(ProductDTO productDTO) {
        // Création: on ignore l'id fourni côté client
        Product product = productMapper.toEntity(productDTO);
        product.setId(null);
        Product savedProduct = productRepository.save(product);
        return productMapper.toDto(savedProduct);
    }

    @Transactional
    @Caching(
        put = @CachePut(value = "product", key = "#id"),
        evict = @CacheEvict(value = "products", allEntries = true)
    )
    public ProductDTO updateProduct(Long id, ProductDTO productDTO) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product " + id + " not found"));
        // Remplacer entièrement (sauf id)
        Product incoming = productMapper.toEntity(productDTO);
        incoming.setId(product.getId());
        Product saved = productRepository.save(incoming);
        return productMapper.toDto(saved);
    }

    @Transactional
    @Caching(
        put = @CachePut(value = "product", key = "#id"),
        evict = @CacheEvict(value = "products", allEntries = true)
    )
    public ProductDTO partialUpdateProduct(Long id, ProductDTO productDTO) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product " + id + " not found"));
        productMapper.updateEntityFromDto(productDTO, product);
        Product saved = productRepository.save(product);
        return productMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public Page<ProductDTO> search(ProductCriteria criteria, Pageable pageable) {
        Specification<Product> spec = buildSpecification(criteria);
        return productRepository.findAll(spec, pageable).map(productMapper::toDto);
    }

    private Specification<Product> buildSpecification(ProductCriteria c) {
        if (c == null) return Specification.where(null);

        Specification<Product> spec = Specification.where(null);

        if (c.getName() != null && !c.getName().isBlank()) {
            String nameLike = "%" + c.getName().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("name")), nameLike));
        }
        if (c.getDescription() != null && !c.getDescription().isBlank()) {
            String descLike = "%" + c.getDescription().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("description")), descLike));
        }
        if (c.getCategory() != null && !c.getCategory().isBlank()) {
            String cat = c.getCategory().toLowerCase();
            spec = spec.and((root, query, cb) -> cb.equal(cb.lower(root.get("category")), cat));
        }
        if (c.getMinPrice() != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("price"), c.getMinPrice()));
        }
        if (c.getMaxPrice() != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("price"), c.getMaxPrice()));
        }
        if (c.getMinQuantity() != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("quantity"), c.getMinQuantity()));
        }
        if (c.getMaxQuantity() != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("quantity"), c.getMaxQuantity()));
        }
        return spec;
    }

    @Transactional(readOnly = true)
    public List<ProductDTO> findByName(String name) {
        return productRepository.findByNameContainingIgnoreCase(name)
                .stream().map(productMapper::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductDTO> findByDescription(String description) {
        return productRepository.findByDescriptionContainingIgnoreCase(description)
                .stream().map(productMapper::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductDTO> findByCategory(String category) {
        return productRepository.findByCategoryIgnoreCase(category)
                .stream().map(productMapper::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductDTO> findByPriceBetween(Double min, Double max) {
        return productRepository.findByPriceBetween(min, max)
                .stream().map(productMapper::toDto).collect(Collectors.toList());
    }

    @Transactional
    @Caching(
        evict = {
            @CacheEvict(value = "product", key = "#id"),
            @CacheEvict(value = "products", allEntries = true)
        }
    )
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }
}
