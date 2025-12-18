package testtask.shift.shopapi.model.analytics;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class StatsInsightsResponse {
    private long totalProducts;
    private long totalStockUnits;
    private BigDecimal totalInventoryValue;
    private List<CategoryMetrics> categories;
}
