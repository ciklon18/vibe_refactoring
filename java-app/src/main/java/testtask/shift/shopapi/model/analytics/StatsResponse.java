package testtask.shift.shopapi.model.analytics;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class StatsResponse {
    private long totalProducts;
    private long laptops;
    private long monitors;
    private long personalComputers;
    private long hardDrives;
    private long totalStockUnits;
}
