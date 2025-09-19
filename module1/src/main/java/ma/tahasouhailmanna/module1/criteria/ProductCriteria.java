package ma.tahasouhailmanna.module1.criteria;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class ProductCriteria {
    private String name;
    private String description;
    private String category;

    @PositiveOrZero
    private Double minPrice;

    @PositiveOrZero
    private Double maxPrice;

    @Min(value = 0, message = "minQuantity must be >= 0")
    private Integer minQuantity;

    @Min(value = 0, message = "maxQuantity must be >= 0")
    private Integer maxQuantity;

    @AssertTrue(message = "minPrice must be <= maxPrice")
    public boolean isPriceRangeValid() {
        return minPrice == null || maxPrice == null || minPrice <= maxPrice;
    }

    @AssertTrue(message = "minQuantity must be <= maxQuantity")
    public boolean isQuantityRangeValid() {
        return minQuantity == null || maxQuantity == null || minQuantity <= maxQuantity;
    }
}
