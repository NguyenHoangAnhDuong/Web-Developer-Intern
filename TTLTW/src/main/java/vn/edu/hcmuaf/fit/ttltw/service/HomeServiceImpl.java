package vn.edu.hcmuaf.fit.ttltw.service;

import vn.edu.hcmuaf.fit.ttltw.dao.HomeDao;
import vn.edu.hcmuaf.fit.ttltw.dao.HomeDaoImpl;

import java.util.*;

public class HomeServiceImpl implements HomeService {
    private final HomeDao homeDao = new HomeDaoImpl();

    @Override
    public List<Map<String, Object>> getFeaturedProducts() {
        List<Map<String, Object>> rawList = homeDao.getFeaturedProducts();
        return groupByProduct(rawList);
    }

    @Override
    public List<Map<String, Object>> getFeaturedAccessories() {
        List<Map<String, Object>> rawList = homeDao.getFeaturedAccessories();
        return groupByProduct(rawList);
    }

    @Override
    public List<Map<String, Object>> getActiveVouchers() {
        return homeDao.getActiveVouchers();
    }

    @Override
    public List<Map<String, Object>> getBannerProducts() {
        List<Map<String, Object>> rawList = homeDao.getBannerProducts();
        return groupByProduct(rawList);
    }

    private List<Map<String, Object>> groupByProduct(List<Map<String, Object>> rawList) {
        Map<Integer, Map<String, Object>> productMap = new LinkedHashMap<>();
        Map<String, Map<String, Object>> variantMap = new LinkedHashMap<>(); // Lưu variants theo product+variant_name

        for (Map<String, Object> row : rawList) {
            Integer productId = (Integer) row.get("id");
            String variantName = String.valueOf(row.get("variant_name")).trim();
            String variantKey = productId + "_" + variantName; // Key duy nhất cho mỗi variant

            // Tạo product nếu chưa tồn tại
            if (!productMap.containsKey(productId)) {
                Map<String, Object> product = new HashMap<>();
                product.put("id", row.get("id"));
                product.put("name", row.get("name"));
                product.put("description", row.get("description"));
                product.put("discount_percentage", row.get("discount_percentage"));
                product.put("total_sold", row.get("total_sold"));
                product.put("warranty_period", row.get("warranty_period"));
                product.put("status", row.get("status"));
                product.put("main_image", row.get("main_image"));
                product.put("category_name", row.get("category_name"));
                product.put("brand_name", row.get("brand_name"));
                product.put("variants", new ArrayList<Map<String, Object>>());
                productMap.put(productId, product);
            }

            // Tạo variant nếu chưa tồn tại
            if (!variantMap.containsKey(variantKey)) {
                Map<String, Object> variant = new HashMap<>();
                variant.put("variant_id", row.get("variant_id"));
                variant.put("variant_name", variantName);
                variant.put("base_price", row.get("base_price"));
                variant.put("variant_color_price", row.get("variant_color_price")); // Thêm giá variant_color
                variant.put("colors", new ArrayList<Map<String, Object>>());
                variantMap.put(variantKey, variant);
                ((List<Map<String, Object>>) productMap.get(productId).get("variants")).add(variant);
            }

            // Thêm color vào variant (tránh duplicate colors)
            Map<String, Object> variant = variantMap.get(variantKey);
            List<Map<String, Object>> colors = (List<Map<String, Object>>) variant.get("colors");

            // Kiểm tra color có tồn tại chưa (dựa trên color_name)
            String colorName = (String) row.get("color_name");
            boolean colorExists = colors.stream()
                    .anyMatch(c -> colorName != null && colorName.equals(c.get("name")));

            if (!colorExists && colorName != null) {
                Map<String, Object> colorObj = new HashMap<>();
                Map<String, Object> colorData = new HashMap<>();
                colorData.put("id", row.get("color_id"));
                colorData.put("name", colorName);
                colorData.put("colorCode", row.get("color_code"));

                colorObj.put("id", row.get("variant_color_id"));
                colorObj.put("color", colorData);
                colorObj.put("price", row.get("variant_color_price"));
                colorObj.put("quantity", row.get("quantity"));
                colorObj.put("sku", row.get("sku"));
                colors.add(colorObj);
            }
        }
        return new ArrayList<>(productMap.values());
    }
}