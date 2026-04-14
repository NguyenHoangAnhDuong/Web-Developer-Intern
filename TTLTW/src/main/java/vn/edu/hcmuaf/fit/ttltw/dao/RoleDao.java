package vn.edu.hcmuaf.fit.ttltw.dao;

import vn.edu.hcmuaf.fit.ttltw.config.DBConnect;
import vn.edu.hcmuaf.fit.ttltw.model.Role;

import java.util.List;
import java.util.Optional;

public class RoleDao {
    private static RoleDao instance;

    public static RoleDao getInstance() {
        if (instance == null) instance = new RoleDao();
        return instance;
    }

    public List<Role> findAll() {
        String sql = """
                SELECT id, name, display_name AS displayName, description,
                       is_system AS isSystem, created_at AS createdAt, updated_at AS updatedAt
                FROM roles
                ORDER BY id
                """;
        return DBConnect.getJdbi().withHandle(h -> h.createQuery(sql).mapToBean(Role.class).list());
    }

    // Trả về các vai trò dành cho nhân viên (loại customer và super_admin)
    public List<Role> findAssignableRoles() {
        String sql = """
                SELECT id, name, display_name AS displayName, description,
                       is_system AS isSystem, created_at AS createdAt, updated_at AS updatedAt
                FROM roles
                WHERE name NOT IN ('customer', 'super_admin')
                ORDER BY id
                """;
        return DBConnect.getJdbi().withHandle(h -> h.createQuery(sql).mapToBean(Role.class).list());
    }

    public Optional<Role> findById(int id) {
        String sql = """
                SELECT id, name, display_name AS displayName, description,
                       is_system AS isSystem, created_at AS createdAt, updated_at AS updatedAt
                FROM roles
                WHERE id = :id
                """;
        return DBConnect.getJdbi().withHandle(h -> h.createQuery(sql).bind("id", id)
                .mapToBean(Role.class).findOne());
    }

    public boolean existsByName(String name) {
        Integer c = DBConnect.getJdbi().withHandle(h -> h
                .createQuery("SELECT COUNT(*) FROM roles WHERE name = :n").bind("n", name)
                .mapTo(Integer.class).one());
        return c > 0;
    }

    public int create(Role role) {
        return DBConnect.getJdbi().withHandle(h -> h
                .createUpdate("INSERT INTO roles(name, display_name, description, is_system) " +
                        "VALUES(:n, :d, :desc, 0)")
                .bind("n", role.getName())
                .bind("d", role.getDisplayName())
                .bind("desc", role.getDescription())
                .executeAndReturnGeneratedKeys("id")
                .mapTo(Integer.class).one());
    }

    public boolean update(int id, String displayName, String description) {
        String sql = "UPDATE roles SET display_name = :d, description = :desc WHERE id = :id AND is_system = 0";
        return DBConnect.getJdbi().withHandle(h -> h.createUpdate(sql)
                .bind("d", displayName).bind("desc", description).bind("id", id).execute()) > 0;
    }

    public boolean delete(int id) {
        // Không xoá vai trò hệ thống và vai trò đang được user sử dụng
        Integer used = DBConnect.getJdbi().withHandle(h -> h
                .createQuery("SELECT COUNT(*) FROM users WHERE roles_id = :id").bind("id", id)
                .mapTo(Integer.class).one());
        if (used > 0) return false;
        return DBConnect.getJdbi().withHandle(h -> h
                .createUpdate("DELETE FROM roles WHERE id = :id AND is_system = 0")
                .bind("id", id).execute()) > 0;
    }

    // Gán lại toàn bộ permissions cho một role
    public void setPermissions(int roleId, List<Integer> permissionIds) {
        DBConnect.getJdbi().useTransaction(h -> {
            h.createUpdate("DELETE FROM role_permissions WHERE role_id = :rid")
                    .bind("rid", roleId).execute();
            if (permissionIds == null || permissionIds.isEmpty()) return;
            var batch = h.prepareBatch("INSERT INTO role_permissions(role_id, permission_id) VALUES(:rid, :pid)");
            for (Integer pid : permissionIds) {
                batch.bind("rid", roleId).bind("pid", pid).add();
            }
            batch.execute();
        });
    }

    public List<Integer> findPermissionIdsByRole(int roleId) {
        return DBConnect.getJdbi().withHandle(h -> h
                .createQuery("SELECT permission_id FROM role_permissions WHERE role_id = :rid")
                .bind("rid", roleId).mapTo(Integer.class).list());
    }
}
