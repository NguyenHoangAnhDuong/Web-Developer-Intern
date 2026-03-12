package vn.edu.hcmuaf.fit.ttltw.dao;

import vn.edu.hcmuaf.fit.ttltw.config.DBConnect;
import vn.edu.hcmuaf.fit.ttltw.model.*;

import java.util.List;

public class VariantDao {

    private static VariantDao instance;

    public static VariantDao getInstance() {
        if (instance == null)
            instance = new VariantDao();
        return instance;
    }

    public VariantDao() {
    }

    // Lấy danh sách màu theo Variant (nút chọn màu)
    public List<VariantColor> getColorsByVariant(int variantId) {
        String sql = """
                    SELECT vc.*,
                           c.id AS c_id, c.name AS c_name, c.color_code
                    FROM variant_colors vc
                    JOIN colors c ON vc.color_id = c.id
                    WHERE vc.variant_id = :vid
                      AND vc.status = 1
                """;
        return DBConnect.getJdbi().withHandle(h -> h.createQuery(sql)
                .bind("vid", variantId)
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
                .list());
    }

    public VariantColor getVariantColorDetail(int variantId, int colorId) {
        String sql = """
                    SELECT vc.*,
                           c.id AS c_id, c.name AS c_name, c.color_code
                    FROM variant_colors vc
                    JOIN colors c ON vc.color_id = c.id
                    WHERE vc.variant_id = :vid
                      AND vc.color_id = :cid
                      AND vc.status = 1
                """;

        return DBConnect.getJdbi().withHandle(h -> h.createQuery(sql)
                .bind("vid", variantId)
                .bind("cid", colorId)
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

    public List<Image> getImagesByVariantColor(int variantColorId) {
        String sql = """
                    SELECT *
                    FROM images
                    WHERE variant_color_id = :vcId
                    ORDER BY is_main DESC, id
                """;
        return DBConnect.getJdbi().withHandle(h -> h.createQuery(sql)
                .bind("vcId", variantColorId)
                .mapToBean(Image.class)
                .list());
    }
}
