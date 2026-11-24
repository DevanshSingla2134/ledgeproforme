package com.ledgepro.dao;

import com.ledgepro.model.DashboardStats;

public interface DashboardDAO {
    DashboardStats getDashboardStats(Long businessId);
}
