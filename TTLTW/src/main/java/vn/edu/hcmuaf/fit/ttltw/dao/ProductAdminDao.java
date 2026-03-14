package vn.edu.hcmuaf.fit.ttltw.dao;

import org.jdbi.v3.core.Handle;
import vn.edu.hcmuaf.fit.ttltw.model.*;

import java.util.List;
import java.util.Map;

public interface ProductAdminDao {

        List<Map<String, Object>> findForAdmin(
                String keyword,
                Integer status,
                Integer categoryId,
                int offset,
                int limit);

        int countForAdmin(String keyword, Integer status, Integer categoryId);

        boolean toggleStatus(int productId);

        List<Map<String, Object>> findForAdminByVariantIds(List<Integer> variantIds);

        List<Integer> findVariantIdsForAdmin(
                String keyword,
                Integer status,
                Integer categoryId,
                int offset,
                int limit);

        Map<String, Object> findProductByVariantColorId(int vcId);

        List<Map<String, Object>> findVariantsByProductId(int productId);

        List<Map<String, Object>> findTechByProductId(int productId);

        List<Map<String, Object>> findColorsByVariantId(int variantId);

        Map<String, Object> findVariantColorDetailForEdit(int vcId);

        Product getProductById(int id);

        List<Product> search(String keyword, Integer status, Integer categoryId);

        void updateStatus(int productId, int status);

        void updateProductBasic(Handle h, Product p);

        void deleteTechSpecsByProductId(Handle h, int productId);

        void updateVariant(Handle h, int variantId, String name, double basePrice);

        void updateVariantColor(Handle h, int vcId, int quantity, String sku, double price);

        Brand findBrandByName(Handle h, String name);

        int insertBrand(Handle h, Brand b);

        int insertProduct(Handle h, Product p);

        int insertVariant(Handle h, int productId, ProductVariant v);

        int insertVariantColor(Handle h, int variantId, VariantColor c);

        void insertVariantColorImage(Handle h, int variantColorId, Image img);

        void insertTechSpec(Handle h, int productId, TechSpecs t);

}
