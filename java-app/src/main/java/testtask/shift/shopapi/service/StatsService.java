package testtask.shift.shopapi.service;

import testtask.shift.shopapi.model.analytics.StatsResponse;
import testtask.shift.shopapi.model.analytics.StatsInsightsResponse;

public interface StatsService {
    StatsResponse getStats();

    StatsInsightsResponse getInsights();
}
