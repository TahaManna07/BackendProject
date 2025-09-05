package ma.tahasouhailmanna.module1.service;

import ma.tahasouhailmanna.module1.criteria.ProductCriteria;
import ma.tahasouhailmanna.module1.dto.ProductDTO;
import ma.tahasouhailmanna.module1.exception.ResourceNotFoundException;
import ma.tahasouhailmanna.module1.mapper.ProductMapper;
import ma.tahasouhailmanna.module1.model.Product;
import ma.tahasouhailmanna.module1.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductService productService;

    private Product product;
    private ProductDTO dto;

    @BeforeEach
    void setUp() {
        product = new Product();
        // setters supposés exister sur l'entité
        product.setId(1L);
        product.setName("P1");
        product.setPrice(100.0);
        product.setDescription("desc");
        product.setImageUrl("img");
        product.setQuantity(5);
        product.setCategory("cat");

        dto = new ProductDTO(
                1L, "P1", 100.0, "desc", "img", 5, "cat"
        );
    }

    @Test
    void getAllProducts_ok() {
        when(productRepository.findAll()).thenReturn(List.of(product));
        when(productMapper.toDTO(product)).thenReturn(dto);

        List<ProductDTO> result = productService.getAllProducts();

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).id());
        verify(productRepository).findAll();
        verify(productMapper).toDTO(product);
    }

    @Test
    void getProductById_ok() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productMapper.toDTO(product)).thenReturn(dto);

        Optional<ProductDTO> result = productService.getProductById(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().id());
    }

    @Test
    void saveProduct_ok() {
        when(productMapper.toEntity(dto)).thenReturn(new Product());
        Product saved = new Product();
        saved.setId(10L);
        when(productRepository.save(any(Product.class))).thenReturn(saved);
        when(productMapper.toDTO(saved)).thenReturn(new ProductDTO(10L, "P1", 100.0, "desc", "img", 5, "cat"));

        ProductDTO created = productService.saveProduct(dto);

        assertEquals(10L, created.id());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void updateProduct_ok() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        Product incoming = new Product();
        when(productMapper.toEntity(dto)).thenReturn(incoming);
        Product saved = new Product();
        saved.setId(1L);
        when(productRepository.save(incoming)).thenReturn(saved);
        when(productMapper.toDTO(saved)).thenReturn(dto);

        ProductDTO updated = productService.updateProduct(1L, dto);

        assertEquals(1L, updated.id());
        verify(productRepository).findById(1L);
        verify(productRepository).save(incoming);
    }

    @Test
    void updateProduct_notFound() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> productService.updateProduct(1L, dto));
        verify(productRepository).findById(1L);
        verify(productRepository, never()).save(any());
    }

    @Test
    void partialUpdateProduct_ok() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        Product saved = new Product();
        saved.setId(1L);
        when(productRepository.save(product)).thenReturn(saved);
        when(productMapper.toDTO(saved)).thenReturn(dto);

        ProductDTO updated = productService.partialUpdateProduct(1L, dto);

        assertEquals(1L, updated.id());
        verify(productMapper).updateEntityFromDto(eq(dto), eq(product));
        verify(productRepository).save(product);
    }

    @Test
    void partialUpdateProduct_notFound() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> productService.partialUpdateProduct(1L, dto));
        verify(productRepository).findById(1L);
    }

    @Test
    void search_ok() {
        PageRequest pr = PageRequest.of(0, 10);
        when(productRepository.findAll(any(Specification.class), eq(pr)))
                .thenReturn(new PageImpl<>(List.of(product), pr, 1));
        when(productMapper.toDTO(product)).thenReturn(dto);

        ProductCriteria criteria = new ProductCriteria();
        criteria.setName("p");

        Page<ProductDTO> page = productService.search(criteria, pr);

        assertEquals(1, page.getTotalElements());
        assertEquals(1L, page.getContent().get(0).id());
        verify(productRepository).findAll(any(Specification.class), eq(pr));
    }

    @Test
    void findByName_ok() {
        when(productRepository.findByNameContainingIgnoreCase("p")).thenReturn(List.of(product));
        when(productMapper.toDTO(product)).thenReturn(dto);

        List<ProductDTO> list = productService.findByName("p");

        assertEquals(1, list.size());
        verify(productRepository).findByNameContainingIgnoreCase("p");
    }

    @Test
    void findByDescription_ok() {
        when(productRepository.findByDescriptionContainingIgnoreCase("d")).thenReturn(List.of(product));
        when(productMapper.toDTO(product)).thenReturn(dto);

        List<ProductDTO> list = productService.findByDescription("d");

        assertEquals(1, list.size());
        verify(productRepository).findByDescriptionContainingIgnoreCase("d");
    }

    @Test
    void findByCategory_ok() {
        when(productRepository.findByCategoryIgnoreCase("cat")).thenReturn(List.of(product));
        when(productMapper.toDTO(product)).thenReturn(dto);

        List<ProductDTO> list = productService.findByCategory("cat");

        assertEquals(1, list.size());
        verify(productRepository).findByCategoryIgnoreCase("cat");
    }

    @Test
    void findByPriceBetween_ok() {
        when(productRepository.findByPriceBetween(10.0, 200.0)).thenReturn(List.of(product));
        when(productMapper.toDTO(product)).thenReturn(dto);

        List<ProductDTO> list = productService.findByPriceBetween(10.0, 200.0);

        assertEquals(1, list.size());
        verify(productRepository).findByPriceBetween(10.0, 200.0);
    }
}
