package ma.tahasouhailmanna.module1.dto;

public record ProductDTO(
        Long id,
        String name,
        Double price,
        String description,
        String imageUrl,
        Integer quantity,
        String category
) {
}
