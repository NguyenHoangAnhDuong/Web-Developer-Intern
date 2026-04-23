package vn.edu.hcmuaf.fit.ttltw.service;

import vn.edu.hcmuaf.fit.ttltw.dao.PermissionDao;

import java.util.Collections;
import java.util.Set;

public class PermissionService {
    private static final PermissionService INSTANCE = new PermissionService();
    public static PermissionService getInstance() { return INSTANCE; }

    private final PermissionDao permissionDao = PermissionDao.getInstance();

    public Set<String> loadUserPermissions(int userId) {
        try {
            return permissionDao.findPermissionNamesByUserId(userId);
        } catch (Exception e) {
            return Collections.emptySet();
        }
    }
}
