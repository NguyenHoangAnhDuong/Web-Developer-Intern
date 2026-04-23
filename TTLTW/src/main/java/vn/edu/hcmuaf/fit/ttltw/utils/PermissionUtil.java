package vn.edu.hcmuaf.fit.ttltw.utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import vn.edu.hcmuaf.fit.ttltw.dao.RoleDao;
import vn.edu.hcmuaf.fit.ttltw.model.Role;
import vn.edu.hcmuaf.fit.ttltw.model.User;
import vn.edu.hcmuaf.fit.ttltw.service.PermissionService;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

public final class PermissionUtil {

    public static final String SUPER_ADMIN_ROLE   = "super_admin";
    public static final String CUSTOMER_ROLE      = "customer";
    public static final String SESSION_PERMISSIONS = "permissions";
    public static final String SESSION_ROLE_NAME   = "roleName";

    private PermissionUtil() {}

    // Nạp permission + tên role vào session
    public static Set<String> loadAndCache(HttpSession session, User user) {
        if (user == null) return Collections.emptySet();
        Set<String> perms = PermissionService.getInstance().loadUserPermissions(user.getId());
        session.setAttribute(SESSION_PERMISSIONS, perms);
        Optional<Role> role = RoleDao.getInstance().findById(user.getRolesId());
        role.ifPresent(r -> session.setAttribute(SESSION_ROLE_NAME, r.getName()));
        return perms;
    }

    @SuppressWarnings("unchecked")
    public static Set<String> getPermissions(HttpSession session) {
        if (session == null) return Collections.emptySet();
        Object p = session.getAttribute(SESSION_PERMISSIONS);
        if (p instanceof Set) return (Set<String>) p;
        return Collections.emptySet();
    }

    public static String getRoleName(HttpSession session) {
        if (session == null) return null;
        Object v = session.getAttribute(SESSION_ROLE_NAME);
        return v != null ? v.toString() : null;
    }

    public static boolean isSuperAdmin(HttpSession session) {
        return SUPER_ADMIN_ROLE.equals(getRoleName(session));
    }

    public static boolean isCustomer(HttpSession session) {
        return CUSTOMER_ROLE.equals(getRoleName(session));
    }

    public static boolean has(HttpServletRequest req, String permission) {
        HttpSession s = req.getSession(false);
        if (s == null) return false;
        if (isSuperAdmin(s)) return true;
        return getPermissions(s).contains(permission);
    }

    public static boolean hasAny(HttpSession session, String... permissions) {
        if (session == null) return false;
        if (isSuperAdmin(session)) return true;
        Set<String> set = getPermissions(session);
        for (String p : permissions) if (set.contains(p)) return true;
        return false;
    }
}
