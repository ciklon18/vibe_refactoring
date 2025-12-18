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
        long laptops = laptopRepository.count();
        long monitors = monitorRepository.count();
        long personalComputers = personalComputerRepository.count();
        long hardDrives = hardDriveRepository.count();

        long totalProducts = laptops + monitors + personalComputers + hardDrives;
        long totalStockUnits = computeTotalStock();

        return new StatsResponse(totalProducts, laptops, monitors, personalComputers, hardDrives, totalStockUnits);
    }

    private long computeTotalStock() {
        return sumStock(laptopRepository.findAll())
                + sumStock(monitorRepository.findAll())
                + sumStock(personalComputerRepository.findAll())
                + sumStock(hardDriveRepository.findAll());
    }

    private long sumStock(Iterable<? extends Product> products) {
        long total = 0L;
        for (Product product : products) {
            if (product.getNumberOfProductsInStock() != null) {
                total += product.getNumberOfProductsInStock();
            }
        }
        return total;
    }
}
