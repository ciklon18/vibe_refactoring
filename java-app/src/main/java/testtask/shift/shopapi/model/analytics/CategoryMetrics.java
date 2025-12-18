package testtask.shift.shopapi.model.analytics;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CategoryMetrics {
    private String category;
    private long count;
    private long stockUnits;
    private BigDecimal averagePrice;
    private BigDecimal inventoryValue;
}
