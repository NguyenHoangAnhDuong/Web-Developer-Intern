package vn.edu.hcmuaf.fit.ttltw.service;

import vn.edu.hcmuaf.fit.ttltw.model.Brand;
import vn.edu.hcmuaf.fit.ttltw.model.Image;
import vn.edu.hcmuaf.fit.ttltw.model.Product;

import java.util.List;
import java.util.Map;

public interface ProductService {

        List<Map<String, Object>> getForAdmin(String keyword, Integer status, Integer categoryId, int page, int limit);

        int countForAdmin(String keyword, Integer status, Integer categoryId);

        boolean toggleStatus(int productId);

        Map<String, Object> getProductForEditByVariantColorId(int vcId);

        List<Map<String, Object>> getProductsForList();

        List<Map<String, Object>> getProductsByCategory(int categoryId);

        List<Map<String, Object>> getProductsByCategoryWithFilters(int categoryId, Double priceMin, Double priceMax,
                        List<String> memory, List<String> colors, Integer year, String brandName,
                        List<String> types, String condition, String sortBy);

        List<Map<String, Object>> getProductsByCategoryPaginated(int categoryId, Double priceMin, Double priceMax,
                        List<String> memory,
                        List<String> colors, Integer year, String brandName, String sortBy, int page, int pageSize,
                        String search);

        int countProductsByCategory(int categoryId, Double priceMin, Double priceMax, List<String> memory,
                        List<String> colors, Integer year, String brandName, String search);

        List<Map<String, Object>> getAccessories();

        List<Map<String, Object>> getAccessoriesWithFilters(
                        Double priceMin,
                        Double priceMax,
                        String brandName,
                        List<String> types,
                        String condition,
                        String sortBy);

        List<Map<String, Object>> getAccessoryCategories();

        // add
        void addProduct(Product product,
                        String[] techNames, String[] techValues, String[] techPriorities,
                        String[] variantNames, String[] basePrices,
                        String[] quantities, String[] variantQuantities,
                        String[] skus, String[] colorVariantIndexes,
                        String[] colorIds, String[] customColors, String[] colorPrices,
                        Map<String, List<Image>> colorImagesMap) throws Exception;

        // edit
        void updateProduct(Product product, String[] techNames, String[] techValues, String[] techPriorities,
                        String[] variantNames, String[] basePrices, String[] quantities,
                        String[] variantIds, String[] colorIds, String[] skus,
                        String[] variantQuantities) throws Exception;


        // Lấy 4 sản phẩm liên quan
        List<Map<String, Object>> getRelatedProducts(
                        int brandId,
                        int excludeProductId,
                        int limit);

    List<String> getSuggestions(String keyword);
    List<Brand> getAll();
}