package ma.tahasouhailmanna.module1.controller;

import ma.tahasouhailmanna.module1.advice.GlobalExceptionHandler;
import ma.tahasouhailmanna.module1.criteria.ProductCriteria;
import ma.tahasouhailmanna.module1.dto.ProductDTO;
import ma.tahasouhailmanna.module1.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@Import(GlobalExceptionHandler.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Autowired
    private ObjectMapper objectMapper;

    private ProductDTO sampleDto(Long id) {
        return new ProductDTO(id, "P1", 100.0, "desc", "img", 5, "cat");
    }

    @Test
    void getAllProducts_ok() throws Exception {
        when(productService.getAllProducts()).thenReturn(List.of(sampleDto(1L), sampleDto(2L)));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)));
    }

    @Test
    void getProductById_found() throws Exception {
        when(productService.getProductById(1L)).thenReturn(Optional.of(sampleDto(1L)));

        mockMvc.perform(get("/api/products/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)));
    }

    @Test
    void getProductById_notFound() throws Exception {
        when(productService.getProductById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/products/{id}", 99))
                .andExpect(status().isNotFound());
    }

    @Test
    void createProduct_ok() throws Exception {
        ProductDTO req = sampleDto(null);
        ProductDTO saved = sampleDto(10L);
        when(productService.saveProduct(any(ProductDTO.class))).thenReturn(saved);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(10)));
    }

    @Test
    void deleteProduct_noContent() throws Exception {
        mockMvc.perform(delete("/api/products/{id}", 1))
                .andExpect(status().isNoContent());
        verify(productService).deleteProduct(1L);
    }

    @Test
    void search_generic_ok() throws Exception {
        when(productService.search(any(ProductCriteria.class), any()))
                .thenReturn(new PageImpl<>(List.of(sampleDto(1L)), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/api/products/search")
                        .param("name", "p")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id", is(1)));
    }

    @Test
    void search_byName_ok() throws Exception {
        when(productService.findByName("p")).thenReturn(List.of(sampleDto(1L)));

        mockMvc.perform(get("/api/products/search/name").param("name", "p"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(1)));
    }

    @Test
    void search_byDescription_ok() throws Exception {
        when(productService.findByDescription("d")).thenReturn(List.of(sampleDto(1L)));

        mockMvc.perform(get("/api/products/search/description").param("description", "d"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(1)));
    }

    @Test
    void search_byCategory_ok() throws Exception {
        when(productService.findByCategory("cat")).thenReturn(List.of(sampleDto(1L)));

        mockMvc.perform(get("/api/products/search/category").param("category", "cat"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(1)));
    }

    @Test
    void search_byPriceBetween_ok() throws Exception {
        when(productService.findByPriceBetween(10.0, 200.0)).thenReturn(List.of(sampleDto(1L)));

        mockMvc.perform(get("/api/products/search/price")
                        .param("min", "10")
                        .param("max", "200"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(1)));
    }

    @Test
    void search_validationError_badRange() throws Exception {
        mockMvc.perform(get("/api/products/search")
                        .param("minPrice", "10")
                        .param("maxPrice", "5"))
                .andExpect(status().isBadRequest());
    }
}
