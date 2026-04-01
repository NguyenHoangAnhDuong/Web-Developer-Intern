package vn.edu.hcmuaf.fit.ttltw.dao;

import org.jdbi.v3.core.Jdbi;
import vn.edu.hcmuaf.fit.ttltw.config.DBConnect;
import vn.edu.hcmuaf.fit.ttltw.model.Product;
import vn.edu.hcmuaf.fit.ttltw.model.ProductVariant;
import vn.edu.hcmuaf.fit.ttltw.model.TechSpecs;
import vn.edu.hcmuaf.fit.ttltw.model.VariantColor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public class ProductUserDaoImpl implements ProductUserDao {
    private final Jdbi jdbi = DBConnect.getJdbi();
    @Override
    public List<Map<String, Object>> getProductsByCategory(int categoryId) {
        return List.of();
    }

    @Override
    public List<Map<String, Object>> getProductsByCategoryWithFilters(int categoryId, Double priceMin, Double priceMax, List<String> memory, List<String> colors, Integer year, String brandName, List<String> types, String condition, String sortBy) {
        return List.of();
    }

    @Override
    public List<Map<String, Object>> getProductsByCategoryPaginated(
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
            String search) {

        int offset = (page - 1) * pageSize;

        StringBuilder sql = new StringBuilder("""
                    SELECT DISTINCT
                        p.id,
                        p.name,
                        p.img AS image,
                        p.discount_percentage AS discount,
                        p.brand_id,
                        p.release_date,
                        p.created_at,
                        v.id AS variant_id,
                        v.name AS variant_name,
                        vc.price AS variant_price,
                        (vc.price * (100 - p.discount_percentage) / 100) AS variant_price_new,
                        vc.id AS variant_color_id,
                        col.name AS color_name,
                        col.color_code,
                        COALESCE(AVG(f.rating), 0) AS rating,
                        p.total_sold AS soldCount
                    FROM products p
                    LEFT JOIN brands b ON p.brand_id = b.id
                    LEFT JOIN product_variants v ON p.id = v.product_id
                    LEFT JOIN variant_colors vc ON v.id = vc.variant_id
                    LEFT JOIN colors col ON vc.color_id = col.id
                    LEFT JOIN feedbacks f ON p.id = f.product_id AND f.status = 1
                    WHERE p.status = 1 AND p.category_id = ?
                """);

        List<Object> params = new ArrayList<>();
        params.add(categoryId);

        // Tìm kiếm theo tên sản phẩm
        if (search != null && !search.trim().isEmpty()) {
            sql.append(" AND p.name LIKE ?");
            params.add("%" + search.trim() + "%");
        }

        // Lọc theo giá
        if (priceMin != null) {
            sql.append(" AND (vc.price * (100 - p.discount_percentage) / 100) >= ?");
            params.add(priceMin);
        }
        if (priceMax != null) {
            sql.append(" AND (vc.price * (100 - p.discount_percentage) / 100) <= ?");
            params.add(priceMax);
        }

        // Lọc theo bộ nhớ
        if (memory != null && !memory.isEmpty()) {
            sql.append(" AND v.name IN (");
            for (int i = 0; i < memory.size(); i++) {
                if (i > 0)
                    sql.append(",");
                sql.append("?");
                params.add(memory.get(i));
            }
            sql.append(")");
        }

        // Lọc theo màu sắc
        if (colors != null && !colors.isEmpty()) {
            sql.append(" AND col.name IN (");
            for (int i = 0; i < colors.size(); i++) {
                if (i > 0)
                    sql.append(",");
                sql.append("?");
                params.add(colors.get(i));
            }
            sql.append(")");
        }

        // Lọc theo năm ra mắt
        if (year != null) {
            sql.append(" AND YEAR(p.release_date) = ?");
            params.add(year);
        }

        // Lọc theo thương hiệu
        if (brandName != null && !brandName.trim().isEmpty()) {
            sql.append(" AND b.name = ?");
            params.add(brandName);
        }

        sql.append("""
                    GROUP BY p.id, p.name, p.img, p.discount_percentage, p.total_sold,
                             p.brand_id, p.release_date, p.created_at,
                             v.id, v.name, vc.price, vc.id, col.name, col.color_code
                """);

        // Sắp xếp
        if (sortBy != null && !sortBy.isEmpty()) {
            switch (sortBy) {
                case "banchay":
                case "Bán chạy":
                    sql.append(" ORDER BY p.total_sold DESC, v.name ASC");
                    break;
                case "giamgia":
                case "Giảm giá":
                    sql.append(" ORDER BY p.discount_percentage DESC, v.name ASC");
                    break;
                case "moi":
                case "Mới":
                    sql.append(" ORDER BY p.created_at DESC, v.name ASC");
                    break;
                case "giatang":
                case "Giá tăng":
                    sql.append(" ORDER BY variant_price_new ASC, v.name ASC");
                    break;
                case "giagiam":
                case "Giá giảm":
                    sql.append(" ORDER BY variant_price_new DESC, v.name ASC");
                    break;
                default:
                    sql.append(" ORDER BY p.id DESC, v.name ASC");
                    break;
            }
        } else {
            sql.append(" ORDER BY p.id DESC, v.name ASC");
        }

        // PAGINATION LIMIT + OFFSET
        sql.append(" LIMIT ? OFFSET ?");
        params.add(pageSize);
        params.add(offset);

        String finalSql = sql.toString();

        return jdbi.withHandle(handle -> {
            var query = handle.createQuery(finalSql);
            for (int i = 0; i < params.size(); i++) {
                query.bind(i, params.get(i));
            }

            List<Map<String, Object>> rawResults = query.map((rs, ctx) -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", rs.getInt("id"));
                map.put("name", rs.getString("name"));
                map.put("image", rs.getString("image"));
                map.put("discount", rs.getInt("discount"));
                map.put("variant_id", rs.getInt("variant_id"));
                map.put("variant_name", rs.getString("variant_name"));
                map.put("variant_price", rs.getDouble("variant_price"));
                map.put("variant_price_new", rs.getDouble("variant_price_new"));
                map.put("variant_color_id", rs.getInt("variant_color_id"));
                map.put("color_name", rs.getString("color_name"));
                map.put("color_code", rs.getString("color_code"));
                map.put("rating", Math.round(rs.getDouble("rating")));
                map.put("soldCount", rs.getInt("soldCount"));
                return map;
            }).list();

            // Group by product - variant - colors
            // getProductsByCategoryWithFilters
            Map<Integer, Map<String, Object>> productMap = new LinkedHashMap<>();
            Map<String, Map<String, Object>> variantMap = new LinkedHashMap<>();

            for (Map<String, Object> row : rawResults) {
                int productId = (int) row.get("id");
                String variantName = (String) row.get("variant_name");
                String variantKey = productId + "_" + (variantName != null ? variantName.trim() : "");

                if (!productMap.containsKey(productId)) {
                    Map<String, Object> product = new HashMap<>();
                    product.put("id", row.get("id"));
                    product.put("name", row.get("name"));
                    product.put("image", row.get("image"));
                    product.put("discount", row.get("discount"));
                    product.put("rating", row.get("rating"));
                    product.put("soldCount", row.get("soldCount"));
                    product.put("variants", new ArrayList<Map<String, Object>>());
                    productMap.put(productId, product);
                }

                if (!variantMap.containsKey(variantKey)) {
                    Map<String, Object> variant = new HashMap<>();
                    variant.put("id", row.get("variant_id"));
                    variant.put("name", variantName);
                    variant.put("priceOld", row.get("variant_price"));
                    variant.put("priceNew", row.get("variant_price_new"));
                    variant.put("colors", new ArrayList<Map<String, Object>>());
                    variantMap.put(variantKey, variant);

                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> variants = (List<Map<String, Object>>) productMap.get(productId)
                            .get("variants");
                    variants.add(variant);
                }

                if (row.get("color_name") != null) {
                    Map<String, Object> color = new HashMap<>();
                    color.put("id", row.get("variant_color_id"));
                    color.put("name", row.get("color_name"));
                    color.put("code", row.get("color_code"));
                    // Thêm giá cho màu từ database
                    color.put("priceNew", row.get("variant_price_new"));
                    color.put("priceOld", row.get("variant_price"));

                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> colors_list = (List<Map<String, Object>>) variantMap.get(variantKey)
                            .get("colors");
                    colors_list.add(color);
                }
            }

            // Set default price
            for (Map<String, Object> product : productMap.values()) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> variants = (List<Map<String, Object>>) product.get("variants");
                if (!variants.isEmpty()) {
                    product.put("priceNew", variants.get(0).get("priceNew"));
                    product.put("priceOld", variants.get(0).get("priceOld"));
                }
            }

            return new ArrayList<>(productMap.values());
        });
    }
    @Override
    public int countProductsByCategory(int categoryId, Double priceMin, Double priceMax, List<String> memory, List<String> colors, Integer year, String brandName, String search) {


            StringBuilder sql = new StringBuilder("""
                    SELECT COUNT(DISTINCT p.id)
                    FROM products p
                    LEFT JOIN brands b ON p.brand_id = b.id
                    LEFT JOIN product_variants v ON p.id = v.product_id
                    LEFT JOIN variant_colors vc ON v.id = vc.variant_id
                    LEFT JOIN colors col ON vc.color_id = col.id
                    WHERE p.status = 1 AND p.category_id = ?
                """);

            List<Object> params = new ArrayList<>();
            params.add(categoryId);

            // Tìm kiếm theo tên sản phẩm
            if (search != null && !search.trim().isEmpty()) {
                sql.append(" AND p.name LIKE ?");
                params.add("%" + search.trim() + "%");
            }

            // Lọc theo giá
            if (priceMin != null) {
                sql.append(" AND (vc.price * (100 - p.discount_percentage) / 100) >= ?");
                params.add(priceMin);
            }
            if (priceMax != null) {
                sql.append(" AND (vc.price * (100 - p.discount_percentage) / 100) <= ?");
                params.add(priceMax);
            }

            // Lọc theo bộ nhớ
            if (memory != null && !memory.isEmpty()) {
                sql.append(" AND v.name IN (");
                for (int i = 0; i < memory.size(); i++) {
                    if (i > 0)
                        sql.append(",");
                    sql.append("?");
                    params.add(memory.get(i));
                }
                sql.append(")");
            }

            // Lọc theo màu sắc
            if (colors != null && !colors.isEmpty()) {
                sql.append(" AND col.name IN (");
                for (int i = 0; i < colors.size(); i++) {
                    if (i > 0)
                        sql.append(",");
                    sql.append("?");
                    params.add(colors.get(i));
                }
                sql.append(")");
            }

            // Lọc theo năm ra mắt
            if (year != null) {
                sql.append(" AND YEAR(p.release_date) = ?");
                params.add(year);
            }

            // Lọc theo thương hiệu
            if (brandName != null && !brandName.trim().isEmpty()) {
                sql.append(" AND b.name = ?");
                params.add(brandName);
            }

            String finalSql = sql.toString();

            return jdbi.withHandle(handle -> {
                var query = handle.createQuery(finalSql);
                for (int i = 0; i < params.size(); i++) {
                    query.bind(i, params.get(i));
                }
                return query.mapTo(Integer.class).one();
            });

    }

    @Override
    public List<Map<String, Object>> getAccessories(){
        String sql = """
                    SELECT DISTINCT c.id, c.name
                    FROM categories c
                    WHERE c.id != 1
                    ORDER BY c.name ASC
                """;

        return jdbi.withHandle(handle -> {
            return handle.createQuery(sql)
                    .map((rs, ctx) -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("id", rs.getInt("id"));
                        map.put("name", rs.getString("name"));
                        return map;
                    })
                    .list();
        });
    }



    @Override
    public List<Map<String, Object>> getAccessoriesWithFilters(Double priceMin, Double priceMax, String brandName, List<String> types, String condition, String sortBy)  {
        StringBuilder sql = new StringBuilder("""
                    SELECT
                        p.id,
                        p.name,
                        p.img AS image,
                        p.category_id,
                        c.name AS category_name,
                        p.discount_percentage AS discount,
                        p.brand_id,
                        p.release_date,
                        p.created_at,
                        v.id AS variant_id,
                        v.name AS variant_name,
                        vc.price AS variant_price,
                        (vc.price * (100 - p.discount_percentage) / 100) AS variant_price_new,
                        vc.id AS variant_color_id,
                        col.name AS color_name,
                        col.color_code,
                        COALESCE(AVG(f.rating), 0) AS rating,
                        p.total_sold AS soldCount
                    FROM products p
                    LEFT JOIN categories c ON p.category_id = c.id
                    LEFT JOIN brands b ON p.brand_id = b.id
                    LEFT JOIN product_variants v ON p.id = v.product_id
                    LEFT JOIN variant_colors vc ON v.id = vc.variant_id
                    LEFT JOIN colors col ON vc.color_id = col.id
                    LEFT JOIN feedbacks f ON p.id = f.product_id AND f.status = 1
                    WHERE p.status = 1 AND p.category_id != 1
                """);

        List<Object> params = new ArrayList<>();

        // Lọc theo giá
        if (priceMin != null) {
            sql.append(" AND (vc.price * (100 - p.discount_percentage) / 100) >= ?");
            params.add(priceMin);
        }
        if (priceMax != null) {
            sql.append(" AND (vc.price * (100 - p.discount_percentage) / 100) <= ?");
            params.add(priceMax);
        }

        // Lọc theo thương hiệu
        if (brandName != null && !brandName.trim().isEmpty()) {
            sql.append(" AND b.name = ?");
            params.add(brandName);
        }

        sql.append("""
                    GROUP BY p.id, p.name, p.img, p.discount_percentage, p.total_sold,
                             p.brand_id, p.release_date, p.created_at,
                             v.id, v.name, vc.price, vc.id, col.name, col.color_code
                """);

        // Sắp xếp
        if (sortBy != null && !sortBy.isEmpty()) {
            switch (sortBy) {
                case "banchay":
                case "Bán chạy":
                    sql.append(" ORDER BY p.total_sold DESC, v.name ASC");
                    break;
                case "giamgia":
                case "Giảm giá":
                    sql.append(" ORDER BY p.discount_percentage DESC, v.name ASC");
                    break;
                case "moi":
                case "Mới":
                    sql.append(" ORDER BY p.created_at DESC, v.name ASC");
                    break;
                case "giatang":
                case "Giá tăng":
                    sql.append(" ORDER BY variant_price_new ASC, v.name ASC");
                    break;
                case "giagiam":
                case "Giá giảm":
                    sql.append(" ORDER BY variant_price_new DESC, v.name ASC");
                    break;
                default:
                    sql.append(" ORDER BY p.id DESC, v.name ASC");
                    break;
            }
        } else {
            sql.append(" ORDER BY p.id DESC, v.name ASC");
        }

        String finalSql = sql.toString();

        return jdbi.withHandle(handle -> {
            var query = handle.createQuery(finalSql);
            for (int i = 0; i < params.size(); i++) {
                query.bind(i, params.get(i));
            }

            List<Map<String, Object>> rawResults = query.map((rs, ctx) -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", rs.getInt("id"));
                map.put("name", rs.getString("name"));
                map.put("image", rs.getString("image"));
                map.put("categoryName", rs.getString("category_name"));
                map.put("discount", rs.getInt("discount"));
                map.put("variant_id", rs.getInt("variant_id"));
                map.put("variant_name", rs.getString("variant_name"));
                map.put("variant_price", rs.getDouble("variant_price"));
                map.put("variant_price_new", rs.getDouble("variant_price_new"));
                map.put("variant_color_id", rs.getInt("variant_color_id"));
                map.put("color_name", rs.getString("color_name"));
                map.put("color_code", rs.getString("color_code"));
                map.put("rating", Math.round(rs.getDouble("rating")));
                map.put("soldCount", rs.getInt("soldCount"));
                return map;
            }).list();

            // Group by product -  variant -  colors
            Map<Integer, Map<String, Object>> productMap = new LinkedHashMap<>();

            for (Map<String, Object> row : rawResults) {
                int productId = (int) row.get("id");

                if (!productMap.containsKey(productId)) {
                    Map<String, Object> product = new HashMap<>();
                    product.put("id", row.get("id"));
                    product.put("name", row.get("name"));
                    product.put("image", row.get("image"));
                    product.put("categoryName", row.get("categoryName"));
                    product.put("discount", row.get("discount"));
                    product.put("rating", row.get("rating"));
                    product.put("soldCount", row.get("soldCount"));
                    product.put("variants", new ArrayList<Map<String, Object>>());
                    productMap.put(productId, product);
                }

                // Add variant info with colors
                Map<String, Object> variant = new HashMap<>();
                variant.put("id", row.get("variant_id"));
                variant.put("name", row.get("variant_name"));
                variant.put("priceOld", row.get("variant_price"));
                variant.put("priceNew", row.get("variant_price_new"));
                variant.put("colors", new ArrayList<Map<String, Object>>());

                @SuppressWarnings("unchecked")
                List<Map<String, Object>> variants = (List<Map<String, Object>>) productMap.get(productId)
                        .get("variants");

                String variantName = (String) row.get("variant_name");
                boolean variantExists = variants.stream()
                        .anyMatch(v -> variantName != null && variantName.equals(v.get("name")));

                if (!variantExists) {
                    variants.add(variant);
                }

                // Add color to variant
                Map<String, Object> currentVariant = variants.stream()
                        .filter(v -> variantName != null && variantName.equals(v.get("name")))
                        .findFirst()
                        .orElse(null);

                if (currentVariant != null) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> variantColors = (List<Map<String, Object>>) currentVariant.get("colors");

                    String colorName = (String) row.get("color_name");
                    boolean colorExists = variantColors.stream()
                            .anyMatch(c -> colorName != null && colorName.equals(c.get("name")));

                    if (!colorExists && colorName != null) {
                        Map<String, Object> color = new HashMap<>();
                        color.put("id", row.get("variant_color_id"));
                        color.put("name", colorName);
                        color.put("code", row.get("color_code"));
                        color.put("priceOld", row.get("variant_price"));
                        color.put("priceNew", row.get("variant_price_new"));
                        variantColors.add(color);
                    }
                }
            }

            // Set default price (first variant)
            for (Map<String, Object> product : productMap.values()) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> variants = (List<Map<String, Object>>) product.get("variants");

                if (!variants.isEmpty()) {
                    product.put("priceNew", variants.get(0).get("priceNew"));
                    product.put("priceOld", variants.get(0).get("priceOld"));
                }
            }

            return new ArrayList<>(productMap.values());
        });
    }

    @Override
    public List<Map<String, Object>> getProductsForList() {
        return List.of();
    }

    @Override
    public Product findProductDetailById(int productId) {
        return null;
    }

    @Override
    public List<ProductVariant> getVariantsByProduct(int productId) {
        return List.of();
    }

    @Override
    public List<TechSpecs> getTechSpecsByProduct(int variantId) {
        return List.of();
    }

    @Override
    public VariantColor getDefaultVariantColor(int productId) {
        return null;
    }


    @Override
    public List<Map<String, Object>> findRelatedBySameBrand(int brandId, int excludeProductId, int limit) {
        return List.of();
    }

    @Override
    public List<Map<String, Object>> findFallbackRelatedProducts(int excludeProductId, List<Integer> excludeIds, int limit) {
        return List.of();
    }

    @Override
    public List<Map<String, Object>> getAccessoryCategories() {
        return List.of();
    }

    @Override
    public List<String> getSuggestions(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return List.of();
        }

        String sql = "SELECT name FROM products WHERE name LIKE :keyword LIMIT 5";

        return jdbi.withHandle(handle ->
                handle.createQuery(sql)
                        .bind("keyword", "%" + keyword.trim() + "%")
                        .mapTo(String.class)
                        .list()
        );
    }
}
