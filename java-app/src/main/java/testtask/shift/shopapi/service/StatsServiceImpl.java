package testtask.shift.shopapi.service;

import org.springframework.stereotype.Service;
import testtask.shift.shopapi.model.Product;
import testtask.shift.shopapi.model.analytics.StatsResponse;
import testtask.shift.shopapi.repository.HardDriveRepository;
import testtask.shift.shopapi.repository.LaptopRepository;
import testtask.shift.shopapi.repository.MonitorRepository;
import testtask.shift.shopapi.repository.PersonalComputerRepository;

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
}
