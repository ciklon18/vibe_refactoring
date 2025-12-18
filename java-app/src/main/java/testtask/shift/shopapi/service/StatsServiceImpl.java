package testtask.shift.shopapi.service;

import org.springframework.stereotype.Service;
import testtask.shift.shopapi.model.Product;
import testtask.shift.shopapi.model.analytics.CategoryMetrics;
import testtask.shift.shopapi.model.analytics.StatsInsightsResponse;
import testtask.shift.shopapi.model.analytics.StatsResponse;
import testtask.shift.shopapi.repository.HardDriveRepository;
import testtask.shift.shopapi.repository.LaptopRepository;
import testtask.shift.shopapi.repository.MonitorRepository;
import testtask.shift.shopapi.repository.PersonalComputerRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class StatsServiceImpl implements StatsService {
    private final LaptopRepository laptopRepository;
    private final MonitorRepository monitorRepository;
    private final PersonalComputerRepository personalComputerRepository;
    private final HardDriveRepository hardDriveRepository;

    public StatsServiceImpl(LaptopRepository laptopRepository,
                            MonitorRepository monitorRepository,
                            PersonalComputerRepository personalComputerRepository,
                            HardDriveRepository hardDriveRepository) {
        this.laptopRepository = laptopRepository;
        this.monitorRepository = monitorRepository;
        this.personalComputerRepository = personalComputerRepository;
        this.hardDriveRepository = hardDriveRepository;
    }

    @Override
    public StatsResponse getStats() {
        CategoryTotals laptopTotals = computeTotals(laptopRepository.findAll());
        CategoryTotals monitorTotals = computeTotals(monitorRepository.findAll());
        CategoryTotals pcTotals = computeTotals(personalComputerRepository.findAll());
        CategoryTotals hddTotals = computeTotals(hardDriveRepository.findAll());

        long totalProducts = laptopTotals.count + monitorTotals.count + pcTotals.count + hddTotals.count;
        long totalStockUnits = laptopTotals.stockUnits + monitorTotals.stockUnits + pcTotals.stockUnits + hddTotals.stockUnits;

        return new StatsResponse(totalProducts, laptopTotals.count, monitorTotals.count, pcTotals.count, hddTotals.count, totalStockUnits);
    }

    @Override
    public StatsInsightsResponse getInsights() {
        CategoryMetrics laptopMetrics = computeMetrics("laptops", laptopRepository.findAll());
        CategoryMetrics monitorMetrics = computeMetrics("monitors", monitorRepository.findAll());
        CategoryMetrics pcMetrics = computeMetrics("personalComputers", personalComputerRepository.findAll());
        CategoryMetrics hddMetrics = computeMetrics("hardDrives", hardDriveRepository.findAll());

        long totalProducts = laptopMetrics.getCount() + monitorMetrics.getCount() + pcMetrics.getCount() + hddMetrics.getCount();
        long totalStockUnits = laptopMetrics.getStockUnits() + monitorMetrics.getStockUnits() + pcMetrics.getStockUnits() + hddMetrics.getStockUnits();
        BigDecimal totalInventoryValue = laptopMetrics.getInventoryValue()
                .add(monitorMetrics.getInventoryValue())
                .add(pcMetrics.getInventoryValue())
                .add(hddMetrics.getInventoryValue());

        return new StatsInsightsResponse(totalProducts, totalStockUnits, totalInventoryValue, List.of(
                laptopMetrics,
                monitorMetrics,
                pcMetrics,
                hddMetrics
        ));
    }

    private CategoryTotals computeTotals(Iterable<? extends Product> products) {
        long count = 0L;
        long stockUnits = 0L;

        for (Product product : products) {
            count++;
            Long stock = product.getNumberOfProductsInStock();
            if (stock != null) {
                stockUnits += stock;
            }
        }

        return new CategoryTotals(count, stockUnits);
    }

    private record CategoryTotals(long count, long stockUnits) {
    }

    private CategoryMetrics computeMetrics(String categoryName, Iterable<? extends Product> products) {
        long count = 0L;
        long stockUnits = 0L;
        BigDecimal totalPrice = BigDecimal.ZERO;
        long priceCount = 0L;
        BigDecimal inventoryValue = BigDecimal.ZERO;

        for (Product product : products) {
            count++;
            Long stock = product.getNumberOfProductsInStock();
            long safeStock = stock != null ? stock : 0L;
            stockUnits += safeStock;

            if (product.getPrice() != null) {
                totalPrice = totalPrice.add(product.getPrice());
                priceCount++;
                if (safeStock > 0) {
                    inventoryValue = inventoryValue.add(product.getPrice().multiply(BigDecimal.valueOf(safeStock)));
                }
            }
        }

        BigDecimal averagePrice = priceCount > 0
                ? totalPrice.divide(BigDecimal.valueOf(priceCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return new CategoryMetrics(categoryName, count, stockUnits, averagePrice, inventoryValue);
    }
}
