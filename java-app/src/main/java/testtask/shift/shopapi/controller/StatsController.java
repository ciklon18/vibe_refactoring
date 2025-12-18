package testtask.shift.shopapi.controller;

import com.sun.istack.NotNull;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import testtask.shift.shopapi.model.analytics.StatsResponse;
import testtask.shift.shopapi.service.StatsService;

@RestController
@RequestMapping("/api/stats")
public class StatsController {
    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @Operation(summary = "Get aggregated shop statistics")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Counts per product type and total stock",
                    content = @Content(schema = @Schema(implementation = StatsResponse.class)))})
    @GetMapping(produces = "application/json")
    public @NotNull StatsResponse getStats() {
        return statsService.getStats();
    }
}
