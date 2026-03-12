package vn.edu.hcmuaf.fit.ttltw.service;

import java.util.List;
import java.util.Map;

public interface HomeService {
    List<Map<String, Object>> getFeaturedProducts();

    List<Map<String, Object>> getFeaturedAccessories();

    List<Map<String, Object>> getActiveVouchers();

    List<Map<String, Object>> getBannerProducts();
}