package vn.edu.hcmuaf.fit.ttltw.service;

import vn.edu.hcmuaf.fit.ttltw.dao.PermissionDao;
import vn.edu.hcmuaf.fit.ttltw.dao.RoleDao;
import vn.edu.hcmuaf.fit.ttltw.model.Permission;
import vn.edu.hcmuaf.fit.ttltw.model.Role;

import java.util.List;
import java.util.Optional;

public class RoleService {
    private final RoleDao roleDao = RoleDao.getInstance();
    private final PermissionDao permissionDao = PermissionDao.getInstance();

    public List<Role> getAllRoles() { return roleDao.findAll(); }
    public List<Role> getAssignableRoles() { return roleDao.findAssignableRoles(); }
    public Optional<Role> getRole(int id) { return roleDao.findById(id); }
    public List<Permission> getAllPermissions() { return permissionDao.findAll(); }
    public List<Integer> getRolePermissionIds(int roleId) { return roleDao.findPermissionIdsByRole(roleId); }

    public int createRole(String name, String displayName, String description, List<Integer> permissionIds) {
        if (name == null || name.isBlank() || displayName == null || displayName.isBlank())
            throw new IllegalArgumentException("Tên vai trò không được trống");
        if (roleDao.existsByName(name))
            throw new IllegalArgumentException("Tên vai trò đã tồn tại");
        Role r = new Role(name.trim(), displayName.trim());
        r.setDescription(description);
        int id = roleDao.create(r);
        roleDao.setPermissions(id, permissionIds);
        return id;
    }

    public boolean updateRole(int id, String displayName, String description, List<Integer> permissionIds) {
        Optional<Role> existing = roleDao.findById(id);
        if (existing.isEmpty()) return false;
        if (existing.get().isSystem() && "super_admin".equals(existing.get().getName())) {
            // Không cho sửa quyền của super_admin
            return false;
        }
        roleDao.update(id, displayName, description);
        roleDao.setPermissions(id, permissionIds);
        return true;
    }

    public boolean deleteRole(int id) { return roleDao.delete(id); }
}
