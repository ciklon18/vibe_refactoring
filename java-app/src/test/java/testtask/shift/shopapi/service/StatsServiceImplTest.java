package testtask.shift.shopapi.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import testtask.shift.shopapi.model.analytics.StatsResponse;
import testtask.shift.shopapi.model.hdd.HardDrive;
import testtask.shift.shopapi.model.laptop.Laptop;
import testtask.shift.shopapi.model.monitor.Monitor;
import testtask.shift.shopapi.model.pc.FormFactor;
import testtask.shift.shopapi.model.pc.PersonalComputer;
import testtask.shift.shopapi.repository.HardDriveRepository;
import testtask.shift.shopapi.repository.LaptopRepository;
import testtask.shift.shopapi.repository.MonitorRepository;
import testtask.shift.shopapi.repository.PersonalComputerRepository;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatsServiceImplTest {
    @Mock
    private LaptopRepository laptopRepository;

    @Mock
    private MonitorRepository monitorRepository;

    @Mock
    private PersonalComputerRepository personalComputerRepository;

    @Mock
    private HardDriveRepository hardDriveRepository;

    @InjectMocks
    private StatsServiceImpl statsService;

    @Test
    void aggregatesCountsAndStockAcrossRepositories() {
        when(laptopRepository.count()).thenReturn(3L);
        when(monitorRepository.count()).thenReturn(2L);
        when(personalComputerRepository.count()).thenReturn(4L);
        when(hardDriveRepository.count()).thenReturn(1L);

        when(laptopRepository.findAll()).thenReturn(List.of(new Laptop(1L, "S1", "Maker", BigDecimal.ONE, 2L, null)));
        when(monitorRepository.findAll()).thenReturn(List.of(new Monitor(2L, "S2", "Maker", BigDecimal.TEN, 3L, 24),
                new Monitor(3L, "S3", "Maker", BigDecimal.TEN, null, 27)));
        when(personalComputerRepository.findAll()).thenReturn(List.of(new PersonalComputer(4L, "S4", "Maker", BigDecimal.TEN, 5L, FormFactor.DESKTOP)));
        when(hardDriveRepository.findAll()).thenReturn(List.of(new HardDrive(5L, "S5", "Maker", BigDecimal.TEN, 7L, 256)));

        StatsResponse stats = statsService.getStats();

        assertThat(stats.getLaptops()).isEqualTo(3);
        assertThat(stats.getMonitors()).isEqualTo(2);
        assertThat(stats.getPersonalComputers()).isEqualTo(4);
        assertThat(stats.getHardDrives()).isEqualTo(1);
        assertThat(stats.getTotalProducts()).isEqualTo(10);
        assertThat(stats.getTotalStockUnits()).isEqualTo(17);
    }
}
