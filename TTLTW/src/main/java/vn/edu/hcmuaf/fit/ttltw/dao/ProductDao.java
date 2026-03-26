package vn.edu.hcmuaf.fit.ttltw.dao;

import org.jdbi.v3.core.Handle;
import vn.edu.hcmuaf.fit.ttltw.model.*;

import java.util.List;
import java.util.Map;

public interface ProductDao {
        List<Map<String, Object>> findForAdmin(
                        String keyword,
                        Integer status,
                        Integer categoryId,
                        int offset,
                        int limit);

        int countForAdmin(String keyword, Integer status, Integer categoryId);

        boolean toggleStatus(int productId);

        Brand findBrandByName(Handle h, String name);

        int insertBrand(Handle h, Brand b);

        int insertProduct(Handle h, Product p);

        int insertVariant(Handle h, int productId, ProductVariant v);

        int insertVariantColor(Handle h, int variantId, VariantColor c);

        void insertVariantColorImage(Handle h, int variantColorId, Image img);

        void insertTechSpec(Handle h, int productId, TechSpecs t);

        List<Map<String, Object>> findForAdminByVariantIds(List<Integer> variantIds);

        // tìm kiếm variant IDs theo từ khóa, trạng thái, danh mục
        List<Integer> findVariantIdsForAdmin(String keyword, Integer status, Integer categoryId, int offset, int limit);

        // edit
        // tìm kiếm sản phẩm theo vcId
        Map<String, Object> findProductByVariantColorId(int vcId);

        // tìm kiếm variant IDs theo productId
        List<Map<String, Object>> findVariantsByProductId(int productId);

        // tìm kiếm tech specs theo productId
        List<Map<String, Object>> findTechByProductId(int productId);

        // tìm kiếm colors theo variantId
        List<Map<String, Object>> findColorsByVariantId(int variantId);

        // tìm kiếm variant color detail theo vcId
        Map<String, Object> findVariantColorDetailForEdit(int vcId);

        Product getProductById(int id);

        // tìm kiếm products theo từ khóa, trạng thái, danh mục
        List<Product> search(String keyword, Integer status, Integer categoryId);

        // cập nhật trạng thái product
        void updateStatus(int productId, int status);

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
                        Integer brandId,
                        List<String> types,
                        String condition,
                        String sortBy);

        List<Map<String, Object>> getAccessoriesWithFilters(
                        Double priceMin,
                        Double priceMax,
                        String brandName,
                        List<String> types,
                        String condition,
                        String sortBy);

        List<Map<String, Object>> getProductsForList();

        void updateProductBasic(Handle h, Product p);

        // Xóa tech specs cũ của product
        void deleteTechSpecsByProductId(Handle h, int productId);

        // Cập nhật variant
        void updateVariant(Handle h, int variantId, String name, double basePrice);

        // cập nhật variant color
        void updateVariantColor(Handle h, int vcId, int quantity, String sku, double price);

        // Thông tin chính product (brand, category)
        Product findProductDetailById(int productId);

        // Variants của product
        List<ProductVariant> getVariantsByProduct(int productId);

        // Colors theo variant
        List<TechSpecs> getTechSpecsByProduct(int variantId);

        // VariantColor mặc định (load trang)
        VariantColor getDefaultVariantColor(int productId);

        // cart
        Map<String, Object> getCartItemDetail(int variantColorId);

        List<Map<String, Object>> findRelatedBySameBrand(
                        int brandId,
                        int excludeProductId,
                        int limit);

        // Lấy sản phẩm bán chạy / mới nhất để bù khi chưa đủ
        List<Map<String, Object>> findFallbackRelatedProducts(
                        int excludeProductId,
                        List<Integer> excludeIds,
                        int limit);

        List<Map<String, Object>> getAccessoryCategories();
        List<Brand> getAll();
}
