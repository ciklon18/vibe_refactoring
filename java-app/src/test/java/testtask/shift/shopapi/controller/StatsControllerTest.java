package testtask.shift.shopapi.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import testtask.shift.shopapi.model.analytics.StatsResponse;
import testtask.shift.shopapi.model.analytics.StatsInsightsResponse;
import testtask.shift.shopapi.model.analytics.CategoryMetrics;
import testtask.shift.shopapi.service.StatsService;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = StatsController.class)
class StatsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StatsService statsService;

    @Test
    void returnsAggregatedStatistics() throws Exception {
        StatsResponse statsResponse = new StatsResponse(10, 3, 2, 4, 1, 17);
        when(statsService.getStats()).thenReturn(statsResponse);

        mockMvc.perform(get("/api/stats").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProducts").value(10))
                .andExpect(jsonPath("$.laptops").value(3))
                .andExpect(jsonPath("$.monitors").value(2))
                .andExpect(jsonPath("$.personalComputers").value(4))
                .andExpect(jsonPath("$.hardDrives").value(1))
                .andExpect(jsonPath("$.totalStockUnits").value(17));
    }

    @Test
    void returnsDetailedInsights() throws Exception {
        StatsInsightsResponse insightsResponse = new StatsInsightsResponse(
                4,
                3,
                new java.math.BigDecimal("40.00"),
                java.util.List.of(
                        new CategoryMetrics("laptops", 2, 3, new java.math.BigDecimal("15.00"), new java.math.BigDecimal("40.00")),
                        new CategoryMetrics("monitors", 1, 0, new java.math.BigDecimal("100.00"), new java.math.BigDecimal("0"))
                )
        );

        when(statsService.getInsights()).thenReturn(insightsResponse);

        mockMvc.perform(get("/api/stats/insights").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProducts").value(4))
                .andExpect(jsonPath("$.categories[0].category").value("laptops"))
                .andExpect(jsonPath("$.categories[0].averagePrice").value(15.00))
                .andExpect(jsonPath("$.categories[0].inventoryValue").value(40.00))
                .andExpect(jsonPath("$.categories[1].stockUnits").value(0));
    }
}
