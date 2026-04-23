package vn.edu.hcmuaf.fit.ttltw.dao;

import vn.edu.hcmuaf.fit.ttltw.config.DBConnect;
import vn.edu.hcmuaf.fit.ttltw.model.Permission;

import java.util.List;
import java.util.Set;
import java.util.HashSet;

public class PermissionDao {
    private static PermissionDao instance;

    public static PermissionDao getInstance() {
        if (instance == null) instance = new PermissionDao();
        return instance;
    }

    public List<Permission> findAll() {
        String sql = """
                SELECT id, name, display_name AS displayName, module, created_at AS createdAt
                FROM permissions
                ORDER BY module, id
                """;
        return DBConnect.getJdbi().withHandle(h -> h.createQuery(sql).mapToBean(Permission.class).list());
    }

    // Lấy tập permission NAME của một user (dựa theo role)
    public Set<String> findPermissionNamesByUserId(int userId) {
        String sql = """
                SELECT p.name
                FROM users u
                JOIN role_permissions rp ON rp.role_id = u.roles_id
                JOIN permissions p        ON p.id = rp.permission_id
                WHERE u.id = :uid
                """;
        List<String> list = DBConnect.getJdbi().withHandle(h -> h.createQuery(sql)
                .bind("uid", userId).mapTo(String.class).list());
        return new HashSet<>(list);
    }
}
