package vn.edu.hcmuaf.fit.ttltw.dao;

import vn.edu.hcmuaf.fit.ttltw.config.DBConnect;
import vn.edu.hcmuaf.fit.ttltw.model.Feedback;

import java.util.List;

public class FeedbackDao {
    /**
     * Lấy danh sách feedback theo productId (chỉ lấy feedback đang hiển thị)
     */
    public List<Feedback> getFeedbacksByProductId(int productId) {
        String sql = """
                    SELECT f.id,
                           f.product_id   AS productId,
                           f.user_id      AS userId,
                           u.username     AS username,
                           f.rating,
                           f.comment,
                           f.status,
                           f.created_at   AS createdAt,
                           f.updated_at   AS updatedAt
                    FROM feedbacks f
                    JOIN users u ON f.user_id = u.id
                    WHERE f.product_id = ?
                      AND f.status = 1
                    ORDER BY f.created_at DESC
                """;

        return DBConnect.getJdbi().withHandle(handle -> handle.createQuery(sql)
                .bind(0, productId)
                .mapToBean(Feedback.class)
                .list());
    }

    public int countByProductId(int productId) {
        String sql = """
                    SELECT COUNT(*)
                    FROM feedbacks
                    WHERE product_id = ?
                      AND status = 1
                """;

        return DBConnect.getJdbi().withHandle(handle -> handle.createQuery(sql)
                .bind(0, productId)
                .mapTo(Integer.class)
                .one());
    }

    public boolean insertFeedback(Feedback feedback) {
        String sql = """
                    INSERT INTO feedbacks (product_id, user_id, rating, comment, status, created_at, updated_at)
                    VALUES (?, ?, ?, ?, 1, NOW(), NOW())
                """;

        return DBConnect.getJdbi().withHandle(handle -> handle.createUpdate(sql)
                .bind(0, feedback.getProductId())
                .bind(1, feedback.getUserId())
                .bind(2, feedback.getRating())
                .bind(3, feedback.getComment())
                .execute()) > 0;
    }

}
