package vn.edu.hcmuaf.fit.ttltw.dao;

import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import vn.edu.hcmuaf.fit.ttltw.config.DBConnect;
import vn.edu.hcmuaf.fit.ttltw.model.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.*;

public class ProductDaoImpl implements ProductDao {
    private final Jdbi jdbi = DBConnect.getJdbi();

    @Override
    public List<Map<String, Object>> findForAdmin(
            String keyword,
            Integer status,
            Integer categoryId,
            int offset,
            int limit) {

        String sql = """
                   SELECT
                   p.id AS p_id, p.name AS p_name, p.img AS p_img,
                    c.id AS c_id, c.name AS c_name,
                   v.id AS v_id, v.name AS v_name, v.base_price, v.status AS v_status,
                    vc.id AS vc_id, vc.price AS vc_price, vc.quantity,
                   vc.status AS vc_status,
                   col.id AS color_id, col.name AS color_name,
                     vc.sku AS vc_sku
                     FROM products p
                    JOIN categories c ON p.category_id = c.id
                    JOIN product_variants v ON p.id = v.product_id
                    JOIN variant_colors vc ON v.id = vc.variant_id
                    JOIN colors col ON vc.color_id = col.id
                    WHERE (:keyword IS NULL OR p.name LIKE CONCAT('%', :keyword, '%'))
                      AND (:status IS NULL OR vc.status = :status)
                      AND (:categoryId IS NULL OR p.category_id = :categoryId)
                    ORDER BY p.id DESC
                    LIMIT :limit OFFSET :offset
                """;

        return jdbi.withHandle(h -> h.createQuery(sql)
                .bind("keyword", keyword)
                .bind("status", status)
                .bind("categoryId", categoryId)
                .bind("limit", limit)
                .bind("offset", offset)
                .mapToMap()
                .list());
    }

    @Override
    public int countForAdmin(String keyword, Integer status, Integer categoryId) {

        String sql = """
                    SELECT COUNT(vc.id)
                    FROM variant_colors vc
                    JOIN product_variants v ON vc.variant_id = v.id
                    JOIN products p ON v.product_id = p.id
                    WHERE (:keyword IS NULL OR p.name LIKE CONCAT('%', :keyword, '%'))
                      AND (:status IS NULL OR vc.status = :status)
                      AND (:categoryId IS NULL OR p.category_id = :categoryId)
                """;

        return jdbi.withHandle(h -> h.createQuery(sql)
                .bind("keyword", keyword)
                .bind("status", status)
                .bind("categoryId", categoryId)
                .mapTo(Integer.class)
                .one());
    }

    @Override
    public boolean toggleStatus(int variantColorId) {
        String sql = """
                    UPDATE variant_colors
                    SET status = CASE WHEN status = 1 THEN 0 ELSE 1 END
                    WHERE id = :id
                """;

        return jdbi.withHandle(h -> {
            int rows = h.createUpdate(sql)
                    .bind("id", variantColorId)
                    .execute();
            return rows > 0;
        });
    }

    @Override
    public int insertBrand(Handle h, Brand b) {
        String sql = """
                    INSERT INTO brands(name)
                    VALUES (:name)
                """;

        return h.createUpdate(sql)
                .bind("name", b.getName())
                .executeAndReturnGeneratedKeys("id")
                .mapTo(Integer.class)
                .one();
    }

    @Override
    public Brand findBrandByName(Handle h, String name) {
        String sql = """
                    SELECT id, name
                    FROM brands
                    WHERE LOWER(name) = LOWER(:name)
                """;

        return h.createQuery(sql)
                .bind("name", name.trim())
                .mapToBean(Brand.class)
                .findOne()
                .orElse(null);
    }

    @Override
    public int insertProduct(Handle h, Product p) {
        String sql = """
                    INSERT INTO products(brand_id,name, img, category_id,discount_percentage, description,warranty_period, release_date, created_at)
                    VALUES (:brandId,:name, :img, :categoryId,:discount, :description, :warranty, :releaseDate,:createdAt)
                """;

        return h.createUpdate(sql)
                .bind("brandId", p.getBrand().getId())
                .bind("name", p.getName())
                .bind("img", p.getMainImage())
                .bind("categoryId", p.getCategory().getId())
                .bind("discount", p.getDiscountPercentage())
                .bind("description", p.getDescription())
                .bind("warranty", 12) // Thiết lập bảo hành cứng 12 tháng (1 năm)
                .bind("releaseDate", LocalDateTime.now()) // Thiết lập ngày ra mắt là lúc thêm sản phẩm
                .bind("createdAt", LocalDateTime.now())
                .executeAndReturnGeneratedKeys("id")
                .mapTo(Integer.class)
                .one();

    }

    @Override
    public int insertVariant(Handle h, int productId, ProductVariant v) {

        String sql = """
                    INSERT INTO product_variants
                    (product_id, name, base_price, status, created_at, updated_at)
                    VALUES (:productId, :name, :basePrice, :status, :createdAt, :updatedAt)
                """;
        return h.createUpdate(sql)
                .bind("productId", productId)
                .bind("name", v.getName())
                .bind("basePrice", v.getBasePrice())
                .bind("status", v.getStatus()) // thường = 1
                .bind("createdAt", LocalDateTime.now())
                .bind("updatedAt", LocalDateTime.now())
                .executeAndReturnGeneratedKeys("id")
                .mapTo(Integer.class)
                .one();
    }

    @Override
    public int insertVariantColor(Handle h, int variantId, VariantColor c) {

        String sql = """
                    INSERT INTO variant_colors (variant_id, color_id, price, quantity, status, sku, created_at)
                    VALUES (:variantId, :colorId, :price, :quantity, 1, :sku, :createdAt)
                """;

        int variantColorId = h.createUpdate(sql)
                .bind("variantId", variantId)
                .bind("colorId", c.getColor().getId())
                .bind("price", c.getPrice())
                .bind("quantity", c.getQuantity())
                .bind("sku", c.getSku())
                .bind("createdAt", LocalDateTime.now())
                .executeAndReturnGeneratedKeys("id")
                .mapTo(Integer.class)
                .one();

        // insert images (nếu có)
        if (c.getImages() != null) {
            for (Image img : c.getImages()) {
                insertVariantColorImage(h, variantColorId, img);
            }
        }

        return variantColorId;
    }

    // IMAGE
    @Override
    public void insertVariantColorImage(Handle h, int variantColorId, Image img) {

        String sql = """
                    INSERT INTO Images
                    (variant_color_id, img_path,is_main, created_at)
                    VALUES (:vcId, :path, :isMain, :createdAt)
                """;

        h.createUpdate(sql)
                .bind("vcId", variantColorId)
                .bind("path", img.getImgPath())
                .bind("isMain", img.isMain() ? 1 : 0) // Chuyển boolean thành int (1 hoặc 0)
                .bind("createdAt", LocalDateTime.now())
                .execute();
    }

    @Override
    public void insertTechSpec(Handle h, int productId, TechSpecs t) {
        String sql = """
                    INSERT INTO  tech_specs
                    (product_id, name, value, priority)
                    VALUES (:productId, :name, :value, :priority)
                """;

        h.createUpdate(sql)
                .bind("productId", productId)
                .bind("name", t.getName())
                .bind("value", t.getValue())
                .bind("priority", t.getPriority())
                .execute();
    }

    @Override
    public List<Map<String, Object>> findForAdminByVariantIds(List<Integer> variantIds) {

        if (variantIds.isEmpty())
            return List.of();
        String sql = """
                    SELECT
                        p.id   AS p_id,   p.name AS p_name, p.img AS p_img,p.category_id  AS category_id,
                        c.id   AS c_id,   c.name AS c_name,
                        v.id   AS v_id,   v.name AS v_name,
                        v.base_price,    vc.status AS vc_status,
                        vc.id  AS vc_id,  vc.price AS vc_price, vc.quantity,
                        col.id AS color_id, col.name AS color_name
                    FROM variant_colors vc
                    JOIN product_variants v ON vc.variant_id = v.id
                    JOIN products p ON v.product_id = p.id
                    JOIN categories c ON p.category_id = c.id
                    JOIN colors col ON vc.color_id = col.id
                    WHERE vc.id IN (<ids>)
                    ORDER BY vc.id DESC
                """;

        return jdbi.withHandle(h -> h.createQuery(sql)
                .bindList("ids", variantIds)
                .mapToMap()
                .list());
    }

    @Override
    public List<Integer> findVariantIdsForAdmin(String keyword, Integer status, Integer categoryId, int offset,
            int limit) {

        String sql = """
                    SELECT vc.id
                    FROM variant_colors vc
                    JOIN product_variants v ON vc.variant_id = v.id
                    JOIN products p ON v.product_id = p.id
                    WHERE (:keyword IS NULL OR p.name LIKE CONCAT('%', :keyword, '%'))
                      AND (:status IS NULL OR vc.status = :status)
                      AND (:categoryId IS NULL OR p.category_id = :categoryId)
                    ORDER BY vc.id DESC
                    LIMIT :limit OFFSET :offset
                """;

        return jdbi.withHandle(h -> h.createQuery(sql)
                .bind("keyword", keyword)
                .bind("status", status)
                .bind("categoryId", categoryId)
                .bind("limit", limit)
                .bind("offset", offset)
                .mapTo(Integer.class)
                .list());
    }

    // edit

    public Map<String, Object> findProductByVariantColorId(int vcId) {

        String sql = """
                    SELECT
                        p.id           AS product_id,
                        p.name         AS product_name,
                        p.img          AS product_img,
                        p.category_id  AS category_id,
                        p.description  AS description,
                        p.discount_percentage
                    FROM variant_colors vc
                    JOIN product_variants v ON vc.variant_id = v.id
                    JOIN products p ON v.product_id = p.id
                    WHERE vc.id = :vcId
                """;

        return jdbi.withHandle(h -> h.createQuery(sql)
                .bind("vcId", vcId)
                .mapToMap()
                .one());
    }

    public List<Map<String, Object>> findVariantsByProductId(int productId) {
        String sql = """
                    SELECT
                        v.id          AS variant_id,
                        v.product_id  AS product_id,
                        v.name        AS variant_name,
                        v.base_price  AS base_price,
                        v.status      AS variant_status
                    FROM product_variants v
                    WHERE v.product_id = :pid
                    ORDER BY v.id
                """;

        return jdbi.withHandle(h -> h.createQuery(sql)
                .bind("pid", productId)
                .mapToMap()
                .list());
    }

    public List<Map<String, Object>> findColorsByVariantId(int variantId) {

        String sql = """
                    SELECT
                        vc.id        AS vc_id,
                        vc.variant_id,
                        vc.color_id,
                        col.name     AS color_name,
                        vc.price     AS color_price,
                        vc.quantity,
                        vc.status    AS vc_status,
                        vc.sku
                    FROM variant_colors vc
                    JOIN colors col ON vc.color_id = col.id
                    WHERE vc.variant_id = :vid
                    ORDER BY vc.id
                """;

        return jdbi.withHandle(h -> h.createQuery(sql)
                .bind("vid", variantId)
                .mapToMap()
                .list());
    }

    public List<Map<String, Object>> findTechByProductId(int productId) {
        String sql = """
                    SELECT
                        t.id          AS tech_id,
                        t.product_id,
                        t.name        AS tech_name,
                        t.value       AS tech_value,
                        t.priority
                    FROM tech_specs t
                    WHERE t.product_id = :pid
                    ORDER BY t.priority
                """;

        return jdbi.withHandle(h -> h.createQuery(sql)
                .bind("pid", productId)
                .mapToMap()
                .list());
    }

    @Override
    public Map<String, Object> findVariantColorDetailForEdit(int vcId) {
        String sql = """
                    SELECT
                        v.id AS variant_id,
                        v.name AS variant_name,
                        v.base_price,
                        p.warranty_period AS warranty,
                        vc.id AS color_id,
                        col.name AS color_name,
                        vc.quantity,
                        vc.sku,
                        vc.price AS color_price
                    FROM variant_colors vc
                    JOIN product_variants v ON vc.variant_id = v.id
                    JOIN products p ON v.product_id = p.id
                    JOIN colors col ON vc.color_id = col.id
                    WHERE vc.id = :vcId
                """;
        return jdbi.withHandle(h -> h.createQuery(sql)
                .bind("vcId", vcId)
                .mapToMap()
                .one());
    }

    @Override
    public void updateStatus(int productId, int status) {
        String sql = "UPDATE products SET status = ? WHERE id = ?";

        try (Connection conn = (Connection) DBConnect.getJdbi();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, status);
            ps.setInt(2, productId);
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Product getProductById(int id) {
        String sql = "SELECT * FROM products WHERE id = ?";
        try (Connection conn = (Connection) DBConnect.getJdbi();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // return mapResult(rs);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Product> search(String keyword, Integer status, Integer categoryId) {
        StringBuilder sql = new StringBuilder("SELECT * FROM products WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND name LIKE ?");
            params.add("%" + keyword + "%");
        }
        if (status != null) {
            sql.append(" AND status = ?");
            params.add(status);
        }
        if (categoryId != null) {
            sql.append(" AND category_id = ?");
            params.add(categoryId);
        }
        sql.append(" ORDER BY id DESC");

        return jdbi.withHandle(handle -> {
            var query = handle.createQuery(sql.toString());
            for (int i = 0; i < params.size(); i++) {
                query.bind(i, params.get(i));
            }
            return query.map((rs, ctx) -> {
                Product p = new Product();
                p.setId(rs.getInt("id"));
                p.setName(rs.getString("name"));
                p.setMainImage(rs.getString("img"));
                p.setDescription(rs.getString("description"));
                p.setDiscountPercentage(rs.getInt("discount_percentage"));
                p.setTotalSold(rs.getInt("total_sold"));
                p.setWarrantyPeriod(rs.getInt("warranty_period"));
                p.setStatus(rs.getInt("status"));
                return p;
            }).list();
        });
    }

    @Override
    public List<Map<String, Object>> getProductsByCategory(int categoryId) {
        String sql = """
                    SELECT
                        p.id,
                        p.name,
                        p.img AS image,
                        p.discount_percentage AS discount,
                        v.id AS variant_id,
                        v.name AS variant_name,
                        vc.price AS variant_price,
                        (vc.price * (100 - p.discount_percentage) / 100) AS variant_price_new,
                        vc.id AS variant_color_id,
                        col.name AS color_name,
                        col.color_code,
                        vc.quantity,
                        vc.sku,
                        COALESCE(AVG(f.rating), 0) AS rating,
                        p.total_sold AS soldCount
                    FROM products p
                    LEFT JOIN product_variants v ON p.id = v.product_id
                    LEFT JOIN variant_colors vc ON v.id = vc.variant_id
                    LEFT JOIN colors col ON vc.color_id = col.id
                    LEFT JOIN feedbacks f ON p.id = f.product_id AND f.status = 1
                    WHERE p.status = 1 AND p.category_id = ?
                    GROUP BY p.id, p.name, p.img, p.discount_percentage, p.total_sold,
                             v.id, v.name, vc.price, vc.id, col.name, col.color_code, vc.quantity, vc.sku
                    ORDER BY p.id DESC, v.name ASC, col.name ASC
                """;

        return jdbi.withHandle(handle -> {
            List<Map<String, Object>> rawResults = handle.createQuery(sql)
                    .bind(0, categoryId)
                    .map((rs, ctx) -> {
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
                        map.put("quantity", rs.getInt("quantity"));
                        map.put("sku", rs.getString("sku"));
                        map.put("rating", Math.round(rs.getDouble("rating")));
                        map.put("soldCount", rs.getInt("soldCount"));
                        return map;
                    }).list();

            // Group by product -> variant -> colors
            Map<Integer, Map<String, Object>> productMap = new LinkedHashMap<>();
            Map<String, Map<String, Object>> variantMap = new LinkedHashMap<>(); // key: productId_variantName

            for (Map<String, Object> row : rawResults) {
                int productId = (int) row.get("id");
                String variantName = (String) row.get("variant_name");
                String variantKey = productId + "_" + (variantName != null ? variantName.trim() : "");

                // Create product if not exist
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

                // Create variant if not exist
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

                // Add color to variant (avoid duplicates)
                Map<String, Object> variant = variantMap.get(variantKey);
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> colors = (List<Map<String, Object>>) variant.get("colors");

                String colorName = (String) row.get("color_name");
                boolean colorExists = colors.stream()
                        .anyMatch(c -> colorName != null && colorName.equals(c.get("name")));

                if (!colorExists && colorName != null) {
                    Map<String, Object> color = new HashMap<>();
                    color.put("id", row.get("variant_color_id"));
                    color.put("name", colorName);
                    color.put("code", row.get("color_code"));
                    color.put("price", row.get("variant_price"));
                    color.put("priceOld", row.get("variant_price"));
                    color.put("priceNew", row.get("variant_price_new"));
                    color.put("quantity", row.get("quantity"));
                    color.put("sku", row.get("sku"));
                    colors.add(color);
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
    public List<Map<String, Object>> getProductsByCategoryWithFilters(
            int categoryId,
            Double priceMin,
            Double priceMax,
            List<String> memory,
            List<String> colors,
            Integer year,
            String brandName,
            List<String> types,
            String condition,
            String sortBy) {
        StringBuilder sql = new StringBuilder("""
                    SELECT
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

        // Lọc theo giá
        if (priceMin != null) {
            sql.append(" AND (vc.price * (100 - p.discount_percentage) / 100) >= ?");
            params.add(priceMin);
        }
        if (priceMax != null) {
            sql.append(" AND (vc.price * (100 - p.discount_percentage) / 100) <= ?");
            params.add(priceMax);
        }

        // Lọc theo bộ nhớ (memory) - cho điện thoại
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

        // Lọc theo loại linh kiện (types) - có thể cần thêm bảng hoặc trường
        // Tạm thời bỏ qua vì chưa rõ cấu trúc

        // Lọc theo tình trạng (condition) - có thể cần thêm trường
        // Tạm thời bỏ qua vì chưa rõ cấu trúc

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
                default: // Nổi bật
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

            // Group by product -> variant -> colors (for filter with color display)
            Map<Integer, Map<String, Object>> productMap = new LinkedHashMap<>();

            for (Map<String, Object> row : rawResults) {
                int productId = (int) row.get("id");

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
    public List<Map<String, Object>> getAccessories() {
        String sql = """
                    SELECT
                        p.id,
                        p.category_id,
                        c.name AS category_name,
                        p.name,
                        p.img AS image,
                        p.discount_percentage AS discount,
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
                    LEFT JOIN product_variants v ON p.id = v.product_id
                    LEFT JOIN variant_colors vc ON v.id = vc.variant_id
                    LEFT JOIN colors col ON vc.color_id = col.id
                    LEFT JOIN feedbacks f ON p.id = f.product_id AND f.status = 1
                    WHERE p.status = 1 AND p.category_id != 1
                    GROUP BY p.id, p.name, p.img, p.discount_percentage, p.total_sold,
                             v.id, v.name, vc.price, vc.id, col.name, col.color_code, c.name
                    ORDER BY p.id DESC, v.name ASC
                """;

        return jdbi.withHandle(handle -> {
            List<Map<String, Object>> rawResults = handle.createQuery(sql)
                    .map((rs, ctx) -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("id", rs.getInt("id"));
                        map.put("categoryId", rs.getInt("category_id"));
                        map.put("categoryName", rs.getString("category_name"));
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
            Map<Integer, Map<String, Object>> productMap = new LinkedHashMap<>();

            for (Map<String, Object> row : rawResults) {
                int productId = (int) row.get("id");

                if (!productMap.containsKey(productId)) {
                    Map<String, Object> product = new HashMap<>();
                    product.put("id", row.get("id"));
                    product.put("categoryId", row.get("categoryId"));
                    product.put("categoryName", row.get("categoryName"));
                    product.put("name", row.get("name"));
                    product.put("image", row.get("image"));
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
    public List<Map<String, Object>> getAccessoriesWithFilters(Double priceMin, Double priceMax, Integer brandId,
            List<String> types, String condition, String sortBy) {
        return List.of();
    }

    @Override
    public List<Map<String, Object>> getAccessoriesWithFilters(
            Double priceMin,
            Double priceMax,
            String brandName,
            List<String> types,
            String condition,
            String sortBy) {
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
        // Placeholder
        return List.of();
    }

    @Override
    public void updateProductBasic(Handle h, Product p) {
        String sql = "UPDATE products SET name = :name, img = :img,discount_percentage = :discount, description = :description WHERE id = :id";
        h.createUpdate(sql)
                .bind("name", p.getName())
                .bind("img", p.getMainImage())
                .bind("discount", p.getDiscountPercentage())
                .bind("description", p.getDescription())
                .bind("id", p.getId())
                .execute();
    }

    @Override
    public void deleteTechSpecsByProductId(Handle h, int productId) {
        h.createUpdate("DELETE FROM tech_specs WHERE product_id = :id")
                .bind("id", productId)
                .execute();
    }

    @Override
    public void updateVariant(Handle h, int variantId, String name, double basePrice) {
        h.createUpdate(
                "UPDATE product_variants SET name = :name, base_price = :price, updated_at = NOW() WHERE id = :id")
                .bind("id", variantId)
                .bind("name", name)
                .bind("price", basePrice)
                .execute();
    }

    @Override
    public void updateVariantColor(Handle h, int vcId, int quantity, String sku, double price) {
        String sql = """
                    UPDATE variant_colors
                    SET quantity = :qty,sku = :sku,price = :price
                    WHERE id = :id
                """;
        h.createUpdate(sql)
                .bind("id", vcId)
                .bind("qty", quantity)
                .bind("sku", sku)
                .bind("price", price)
                .execute();
    }

    public List<Map<String, Object>> getProductsForListDisplay() {
        String sql = """
                    SELECT
                        p.id,
                        p.name,
                        p.img AS image,
                        p.discount_percentage AS discount,
                        MIN(vc.price) AS priceOld,
                        MIN(vc.price * (100 - p.discount_percentage) / 100) AS priceNew,
                        MAX(vc.price) AS maxPriceOld,
                        MAX(vc.price * (100 - p.discount_percentage) / 100) AS maxPriceNew,
                        GROUP_CONCAT(DISTINCT v.name ORDER BY v.name SEPARATOR ', ') AS capacities,
                        COALESCE(AVG(f.rating), 0) AS rating,
                        p.total_sold AS soldCount
                    FROM products p
                    LEFT JOIN product_variants v ON p.id = v.product_id
                    LEFT JOIN variant_colors vc ON v.id = vc.variant_id
                    LEFT JOIN feedbacks f ON p.id = f.product_id AND f.status = 1
                    WHERE p.status = 1
                    GROUP BY p.id, p.name, p.img, p.discount_percentage, p.total_sold
                    ORDER BY p.id DESC
                """;

        return jdbi.withHandle(handle -> handle.createQuery(sql)
                .map((rs, ctx) -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", rs.getInt("id"));
                    map.put("name", rs.getString("name"));
                    map.put("image", rs.getString("image"));
                    map.put("discount", rs.getInt("discount"));

                    // Giá cũ (giá gốc)
                    double priceOld = rs.getDouble("priceOld");
                    map.put("priceOld", priceOld);

                    // Giá mới (đã giảm giá)
                    double priceNew = rs.getDouble("priceNew");
                    map.put("priceNew", priceNew);

                    String capacitiesStr = rs.getString("capacities");
                    if (capacitiesStr != null && !capacitiesStr.isEmpty()) {
                        map.put("capacities", Arrays.asList(capacitiesStr.split(", ")));
                    } else {
                        map.put("capacities", new ArrayList<>());
                    }

                    map.put("rating", Math.round(rs.getDouble("rating")));
                    map.put("soldCount", rs.getInt("soldCount"));
                    return map;
                }).list());
    }

    @Override
    // Lấy thông tin Product chính
    public Product findProductDetailById(int productId) {
        String sql = """
                    SELECT p.*,
                           c.id AS c_id, c.name AS c_name,
                           b.id AS b_id, b.name AS b_name
                    FROM products p
                    LEFT JOIN categories c ON p.category_id = c.id
                    LEFT JOIN brands b ON p.brand_id = b.id
                    WHERE p.id = ?
                """;

        return jdbi.withHandle(handle -> handle.createQuery(sql)
                .bind(0, productId)
                .map((rs, ctx) -> {
                    Product p = new Product();
                    p.setId(rs.getInt("id"));
                    p.setName(rs.getString("name"));
                    p.setDescription(rs.getString("description"));
                    p.setMainImage(rs.getString("img"));
                    p.setDiscountPercentage(rs.getInt("discount_percentage"));
                    p.setTotalSold(rs.getInt("total_sold"));
                    p.setWarrantyPeriod(rs.getInt("warranty_period"));
                    p.setStatus(rs.getInt("status"));
                    p.setReleaseDate(rs.getTimestamp("release_date") != null
                            ? rs.getTimestamp("release_date").toLocalDateTime()
                            : null);

                    Category c = new Category();
                    c.setId(rs.getInt("c_id"));
                    c.setName(rs.getString("c_name"));
                    p.setCategory(c);

                    Brand b = new Brand();
                    b.setId(rs.getInt("b_id"));
                    b.setName(rs.getString("b_name"));
                    p.setBrand(b);

                    return p;
                }).findOne().orElse(null));
    }

    @Override
    // Danh sách Variant theo Product
    public List<ProductVariant> getVariantsByProduct(int productId) {
        String sql = """
                    SELECT id, name, base_price, status, created_at, updated_at
                    FROM product_variants
                    WHERE product_id = ? AND status = 1
                    ORDER BY base_price
                """;

        return jdbi.withHandle(handle -> handle.createQuery(sql)
                .bind(0, productId)
                .map((rs, ctx) -> {
                    ProductVariant v = new ProductVariant();
                    v.setId(rs.getInt("id"));
                    v.setName(rs.getString("name"));
                    v.setBasePrice(rs.getDouble("base_price"));
                    v.setStatus(1);
                    v.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    v.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                    return v;
                }).list());
    }

    @Override
    // Lấy Tech Specs theo product
    public List<TechSpecs> getTechSpecsByProduct(int productId) {
        String sql = """
                    SELECT *
                    FROM tech_specs
                    WHERE product_id = ?
                    ORDER BY priority
                """;

        return jdbi.withHandle(handle -> handle.createQuery(sql)
                .bind(0, productId)
                .map((rs, ctx) -> {
                    TechSpecs t = new TechSpecs();
                    t.setId(rs.getInt("id"));
                    t.setProductId(rs.getInt("product_id"));
                    t.setName(rs.getString("name"));
                    t.setValue(rs.getString("value"));
                    t.setPriority(rs.getInt("priority"));
                    t.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    t.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                    return t;
                }).list());
    }

    @Override
    public VariantColor getDefaultVariantColor(int productId) {
        String sql = """
                    SELECT vc.*,
                           c.id AS c_id, c.name AS c_name, c.color_code
                    FROM variant_colors vc
                    JOIN product_variants v ON vc.variant_id = v.id
                    JOIN colors c ON vc.color_id = c.id
                    WHERE v.product_id = ?
                      AND v.status = 1
                      AND vc.status = 1
                    ORDER BY v.id ASC, vc.id ASC
                    LIMIT 1
                """;

        return jdbi.withHandle(h -> h.createQuery(sql)
                .bind(0, productId)
                .map((rs, ctx) -> {
                    VariantColor vc = new VariantColor();
                    vc.setId(rs.getInt("id"));
                    vc.setPrice(rs.getDouble("price"));
                    vc.setQuantity(rs.getInt("quantity"));
                    vc.setSku(rs.getString("sku"));
                    vc.setStatus(rs.getInt("status"));

                    Color color = new Color();
                    color.setId(rs.getInt("c_id"));
                    color.setName(rs.getString("c_name"));
                    color.setColorCode(rs.getString("color_code"));
                    vc.setColor(color);

                    return vc;
                })
                .findOne()
                .orElse(null));
    }

    // Lấy tất cả variant_colors với variantId và colorId để JSP hiển thị giá
    public List<Map<String, Object>> getAllVariantColorsForProduct(int productId) {
        String sql = """
                    SELECT
                        vc.id AS vc_id,
                        vc.variant_id,
                        vc.color_id,
                        vc.price,
                        vc.quantity,
                        v.id AS variant_id_from_v,
                        v.base_price
                    FROM variant_colors vc
                    JOIN product_variants v ON vc.variant_id = v.id
                    WHERE v.product_id = :pid
                      AND v.status = 1
                      AND vc.status = 1
                    ORDER BY v.id, vc.id
                """;

        return jdbi.withHandle(h -> h.createQuery(sql)
                .bind("pid", productId)
                .map((rs, ctx) -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", rs.getInt("vc_id"));
                    map.put("variantId", rs.getInt("variant_id"));
                    map.put("colorId", rs.getInt("color_id"));
                    map.put("price", rs.getDouble("price"));
                    map.put("quantity", rs.getInt("quantity"));
                    map.put("basePrice", rs.getDouble("base_price"));
                    return map;
                })
                .list());
    }

    @Override
    public List<Map<String, Object>> findRelatedBySameBrand(
            int brandId,
            int excludeProductId,
            int limit) {
        String sql = """
                    SELECT
                        p.id,
                        p.name,
                        p.img AS image,
                        p.discount_percentage AS discount,
                        p.total_sold AS soldCount,
                        p.release_date,
                        v.id AS variant_id,
                        v.name AS variant_name,
                        vc.price AS variant_price,
                        (vc.price * (100 - p.discount_percentage) / 100) AS variant_price_new,
                        COALESCE(AVG(f.rating), 0) AS rating
                    FROM products p
                    LEFT JOIN product_variants v ON p.id = v.product_id
                    LEFT JOIN variant_colors vc ON v.id = vc.variant_id
                    LEFT JOIN feedbacks f ON p.id = f.product_id AND f.status = 1
                    WHERE p.status = 1
                      AND p.brand_id = :brandId
                      AND p.id <> :excludeId
                    GROUP BY p.id, p.name, p.img, p.discount_percentage,
                             p.total_sold, p.release_date,
                             v.id, v.name, vc.price
                    ORDER BY p.total_sold DESC, p.release_date DESC
                    LIMIT :limit
                """;

        return jdbi.withHandle(handle -> {
            List<Map<String, Object>> rawResults = handle.createQuery(sql)
                    .bind("brandId", brandId)
                    .bind("excludeId", excludeProductId)
                    .bind("limit", limit)
                    .map((rs, ctx) -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("id", rs.getInt("id"));
                        map.put("name", rs.getString("name"));
                        map.put("image", rs.getString("image"));
                        map.put("discount", rs.getInt("discount"));
                        map.put("rating", Math.round(rs.getDouble("rating")));
                        map.put("soldCount", rs.getInt("soldCount"));
                        map.put("variant_id", rs.getInt("variant_id"));
                        map.put("variant_name", rs.getString("variant_name"));
                        map.put("variant_price", rs.getDouble("variant_price"));
                        map.put("variant_price_new", rs.getDouble("variant_price_new"));
                        return map;
                    }).list();

            return groupProductVariants(rawResults);
        });
    }

    @Override
    public List<Map<String, Object>> findFallbackRelatedProducts(
            int excludeProductId,
            List<Integer> excludeIds,
            int limit) {
        String sql = """
                    SELECT
                        p.id,
                        p.name,
                        p.img AS image,
                        p.discount_percentage AS discount,
                        p.total_sold AS soldCount,
                        p.release_date,
                        v.id AS variant_id,
                        v.name AS variant_name,
                        vc.price AS variant_price,
                        (vc.price * (100 - p.discount_percentage) / 100) AS variant_price_new,
                        COALESCE(AVG(f.rating), 0) AS rating
                    FROM products p
                    LEFT JOIN product_variants v ON p.id = v.product_id
                    LEFT JOIN variant_colors vc ON v.id = vc.variant_id
                    LEFT JOIN feedbacks f ON p.id = f.product_id AND f.status = 1
                    WHERE p.status = 1
                      AND p.id <> :excludeId
                      AND p.id NOT IN (<excludeIds>)
                    GROUP BY p.id, p.name, p.img, p.discount_percentage,
                             p.total_sold, p.release_date,
                             v.id, v.name, vc.price
                    ORDER BY p.total_sold DESC, p.release_date DESC
                    LIMIT :limit
                """;

        return jdbi.withHandle(handle -> {
            List<Map<String, Object>> rawResults = handle.createQuery(sql)
                    .bind("excludeId", excludeProductId)
                    .bindList("excludeIds", excludeIds.isEmpty() ? List.of(-1) : excludeIds)
                    .bind("limit", limit)
                    .map((rs, ctx) -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("id", rs.getInt("id"));
                        map.put("name", rs.getString("name"));
                        map.put("image", rs.getString("image"));
                        map.put("discount", rs.getInt("discount"));
                        map.put("rating", Math.round(rs.getDouble("rating")));
                        map.put("soldCount", rs.getInt("soldCount"));
                        map.put("variant_id", rs.getInt("variant_id"));
                        map.put("variant_name", rs.getString("variant_name"));
                        map.put("variant_price", rs.getDouble("variant_price"));
                        map.put("variant_price_new", rs.getDouble("variant_price_new"));
                        return map;
                    }).list();

            return groupProductVariants(rawResults);
        });
    }

    private List<Map<String, Object>> groupProductVariants(List<Map<String, Object>> rawResults) {
        Map<Integer, Map<String, Object>> productMap = new LinkedHashMap<>();

        for (Map<String, Object> row : rawResults) {
            int productId = (int) row.get("id");

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

            Map<String, Object> variant = new HashMap<>();
            variant.put("id", row.get("variant_id"));
            variant.put("name", row.get("variant_name"));
            variant.put("priceOld", row.get("variant_price"));
            variant.put("priceNew", row.get("variant_price_new"));

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> variants = (List<Map<String, Object>>) productMap.get(productId).get("variants");
            variants.add(variant);
        }

        // set giá mặc định
        for (Map<String, Object> product : productMap.values()) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> variants = (List<Map<String, Object>>) product.get("variants");

            if (!variants.isEmpty()) {
                product.put("priceNew", variants.get(0).get("priceNew"));
                product.put("priceOld", variants.get(0).get("priceOld"));
            }
        }

        return new ArrayList<>(productMap.values());
    }

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


    public int countProductsByCategory(
            int categoryId,
            Double priceMin,
            Double priceMax,
            List<String> memory,
            List<String> colors,
            Integer year,
            String brandName,
            String search) {

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


//      Lấy danh sách tất cả categories có category_id khác 1 (dành cho linh kiện)

    public List<Map<String, Object>> getAccessoryCategories() {
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

}