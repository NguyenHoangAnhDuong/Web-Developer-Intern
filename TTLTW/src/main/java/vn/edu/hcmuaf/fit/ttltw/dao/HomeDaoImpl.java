package vn.edu.hcmuaf.fit.ttltw.dao;

import org.jdbi.v3.core.Jdbi;
import vn.edu.hcmuaf.fit.ttltw.config.DBConnect;
import java.util.List;
import java.util.Map;

public class HomeDaoImpl implements HomeDao {
    private final Jdbi jdbi = DBConnect.getJdbi();

    @Override
    public List<Map<String, Object>> getFeaturedProducts() {
        String sql = """
                SELECT DISTINCT
                    p.id, p.name, p.description, p.discount_percentage,
                    p.total_sold, p.warranty_period, p.status, p.img AS main_image,
                    c.name AS category_name,
                    b.name AS brand_name,
                    pv.id AS variant_id, pv.name AS variant_name, pv.base_price,
                    vc.price AS variant_color_price, vc.quantity, vc.sku, vc.id AS variant_color_id,
                    col.id AS color_id, col.name AS color_name, col.color_code
                FROM (
                    SELECT p.*
                    FROM products p
                    WHERE p.category_id = 1 AND p.status = 1
                        AND EXISTS (
                            SELECT 1
                            FROM product_variants pv2
                            JOIN variant_colors vc2 ON pv2.id = vc2.variant_id
                            WHERE pv2.product_id = p.id AND pv2.status = 1 AND vc2.status = 1 AND vc2.quantity > 0 )
                    ORDER BY p.id DESC
                    LIMIT 4
                ) p
                LEFT JOIN categories c ON p.category_id = c.id
                LEFT JOIN brands b ON p.brand_id = b.id
                LEFT JOIN product_variants pv ON p.id = pv.product_id
                LEFT JOIN variant_colors vc ON pv.id = vc.variant_id
                LEFT JOIN colors col ON vc.color_id = col.id
                ORDER BY p.id DESC, pv.id, col.id
                """;

        return jdbi.withHandle(h -> h.createQuery(sql).mapToMap().list());
    }

    @Override
    public List<Map<String, Object>> getFeaturedAccessories() {
        String sql = """
                SELECT DISTINCT
                    p.id, p.name, p.description, p.discount_percentage,
                    p.total_sold, p.warranty_period, p.status, p.img AS main_image,
                    c.name AS category_name,
                    b.name AS brand_name,
                    pv.id AS variant_id, pv.name AS variant_name, pv.base_price,
                    vc.price AS variant_color_price, vc.quantity, vc.sku, vc.id AS variant_color_id,
                    col.id AS color_id, col.name AS color_name, col.color_code
                FROM (
                    SELECT p.*
                    FROM products p
                    WHERE p.category_id != 1 AND p.status = 1
                        AND EXISTS (
                            SELECT 1
                            FROM product_variants pv2
                            JOIN variant_colors vc2 ON pv2.id = vc2.variant_id
                            WHERE pv2.product_id = p.id AND pv2.status = 1 AND vc2.status = 1 AND vc2.quantity > 0 )
                    ORDER BY p.release_date DESC
                    LIMIT 4
                ) p
                LEFT JOIN categories c ON p.category_id = c.id
                LEFT JOIN brands b ON p.brand_id = b.id
                LEFT JOIN product_variants pv ON p.id = pv.product_id
                LEFT JOIN variant_colors vc ON pv.id = vc.variant_id
                LEFT JOIN colors col ON vc.color_id = col.id
                ORDER BY p.id DESC, pv.id, col.id
                """;

        return jdbi.withHandle(h -> h.createQuery(sql).mapToMap().list());
    }

    @Override
    public List<Map<String, Object>> getActiveVouchers() {
        String sql = """
                SELECT id, voucher_code AS name, discount_amount AS discount_value,
                       type AS discount_type, min_order_value, max_reduce AS max_discount,
                       quantity, start_date, end_date, status
                FROM vouchers
                WHERE status = 1
                ORDER BY id DESC
                LIMIT 10
                """;

        return jdbi.withHandle(h -> h.createQuery(sql).mapToMap().list());
    }

    @Override
    public List<Map<String, Object>> getBannerProducts() {
        String sql = """
                SELECT DISTINCT
                    p.id, p.name, p.description, p.discount_percentage,
                    p.total_sold, p.warranty_period, p.status, p.img AS main_image,
                    c.name AS category_name,
                    b.name AS brand_name,
                    pv.id AS variant_id, pv.name AS variant_name, pv.base_price,
                    vc.price AS variant_color_price, vc.quantity, vc.sku, vc.id AS variant_color_id,
                    col.id AS color_id, col.name AS color_name, col.color_code
                FROM (
                    SELECT p.*
                    FROM products p
                    WHERE p.status = 1
                        AND EXISTS (
                            SELECT 1
                            FROM product_variants pv2
                            JOIN variant_colors vc2 ON pv2.id = vc2.variant_id
                            WHERE pv2.product_id = p.id AND pv2.status = 1 AND vc2.status = 1 AND vc2.quantity > 0 )
                    ORDER BY p.id DESC, p.total_sold DESC
                    LIMIT 4
                ) p
                LEFT JOIN categories c ON p.category_id = c.id
                LEFT JOIN brands b ON p.brand_id = b.id
                LEFT JOIN product_variants pv ON p.id = pv.product_id
                LEFT JOIN variant_colors vc ON pv.id = vc.variant_id
                LEFT JOIN colors col ON vc.color_id = col.id
                ORDER BY p.id DESC, pv.id, col.id
                """;

        return jdbi.withHandle(h -> h.createQuery(sql).mapToMap().list());
    }
}