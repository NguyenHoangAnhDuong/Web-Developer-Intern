package vn.edu.hcmuaf.fit.ttltw.dao;

import vn.edu.hcmuaf.fit.ttltw.model.Product;
import vn.edu.hcmuaf.fit.ttltw.model.ProductVariant;
import vn.edu.hcmuaf.fit.ttltw.model.TechSpecs;
import vn.edu.hcmuaf.fit.ttltw.model.VariantColor;

import java.util.List;
import java.util.Map;

public interface ProductUserDao {
    List<Map<String, Object>> getProductsByCategory(int categoryId);

    List<Map<String, Object>> getProductsByCategoryWithFilters(
            int categoryId,
            Double priceMin,
            Double priceMax,
            List<String> memory,
            List<String> colors,
            Integer year,
            String brandName,
            List<String> types,
            String condition,
            String sortBy);

    List<Map<String, Object>> getProductsByCategoryPaginated(
            int categoryId,
            Double priceMin,
            Double priceMax,
            List<String> memory,
            List<String> colors,
            Integer year,
            String brandName,
            String sortBy,
            int page,
            int pageSize,
            String search);

    int countProductsByCategory(
            int categoryId,
            Double priceMin,
            Double priceMax,
            List<String> memory,
            List<String> colors,
            Integer year,
            String brandName,
            String search);

    List<Map<String, Object>> getAccessories();



    List<Map<String, Object>> getAccessoriesWithFilters(
            Double priceMin,
            Double priceMax,
            String brandName,
            List<String> types,
            String condition,
            String sortBy);

    List<Map<String, Object>> getProductsForList();

    Product findProductDetailById(int productId);

    List<ProductVariant> getVariantsByProduct(int productId);

    List<TechSpecs> getTechSpecsByProduct(int variantId);

    VariantColor getDefaultVariantColor(int productId);

    Map<String, Object> getCartItemDetail(int variantColorId);

    List<Map<String, Object>> findRelatedBySameBrand(
            int brandId,
            int excludeProductId,
            int limit);

    List<Map<String, Object>> findFallbackRelatedProducts(
            int excludeProductId,
            List<Integer> excludeIds,
            int limit);

    List<Map<String, Object>> getAccessoryCategories();
}
