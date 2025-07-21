package com.store.auth.config;

import com.store.auth.entity.Privilege;
import com.store.auth.entity.Role;
import com.store.auth.entity.Tenant;
import com.store.auth.entity.User;
import com.store.auth.repository.PrivilegeRepository;
import com.store.auth.repository.RoleRepository;
import com.store.auth.repository.TenantRepository;
import com.store.auth.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Initializes sample data for testing the Auth Service.
 * Creates default tenants, roles, privileges, and users on application startup.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final TenantRepository tenantRepository;
    private final RoleRepository roleRepository;
    private final PrivilegeRepository privilegeRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(TenantRepository tenantRepository, 
                          RoleRepository roleRepository,
                          PrivilegeRepository privilegeRepository,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder) {
        this.tenantRepository = tenantRepository;
        this.roleRepository = roleRepository;
        this.privilegeRepository = privilegeRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        // Only initialize if data doesn't exist
        if (tenantRepository.count() == 0) {
            initTenants();
        }
        
        if (privilegeRepository.count() == 0) {
            initPrivileges();
        }
        
        if (roleRepository.count() == 0) {
            initRoles();
        }
        
        if (userRepository.count() == 0) {
            initUsers();
        }
    }

    private void initTenants() {
        // Create brand tenant (parent)
        Tenant brand = Tenant.builder()
                .name("Sample Brand")
                .type("brand")
                .build();
        brand = tenantRepository.save(brand);

        // Create store/outlet tenants (children)
        Tenant store1 = Tenant.builder()
                .name("Store One")
                .type("store")
                .parentTenant(brand)
                .build();

        Tenant store2 = Tenant.builder()
                .name("Store Two")
                .type("store")
                .parentTenant(brand)
                .build();

        tenantRepository.saveAll(Arrays.asList(store1, store2));
    }

    private void initPrivileges() {
        List<String> privilegeNames = Arrays.asList(
                "USER_VIEW", "USER_VIEW_ALL", "USER_CREATE", "USER_EDIT", "USER_DELETE",
                "ROLE_VIEW", "ROLE_VIEW_ALL", "ROLE_CREATE", "ROLE_EDIT", "ROLE_DELETE",
                "INVENTORY_VIEW", "INVENTORY_VIEW_ALL", "INVENTORY_CREATE", "INVENTORY_EDIT", "INVENTORY_DELETE",
                "ORDER_VIEW", "ORDER_VIEW_ALL", "ORDER_CREATE", "ORDER_EDIT", "ORDER_DELETE",
                "TENANT_VIEW", "TENANT_VIEW_ALL", "TENANT_CREATE", "TENANT_EDIT", "TENANT_DELETE"
        );

        List<Privilege> privileges = new ArrayList<>();
        for (String name : privilegeNames) {
            Privilege privilege = Privilege.builder()
                    .name(name)
                    .description("Permission to " + name.toLowerCase().replace("_", " "))
                    .build();
            privileges.add(privilege);
        }
        
        privilegeRepository.saveAll(privileges);
    }

    private void initRoles() {
        // Get all privileges
        List<Privilege> allPrivileges = privilegeRepository.findAll();
        
        // Create Super Admin role with all privileges
        Role superAdmin = Role.builder()
                .name("SUPER_ADMIN")
                .description("Super administrator with all privileges")
                .privileges(new HashSet<>(allPrivileges))
                .build();
        
        // Create Store Manager role with limited privileges
        Set<Privilege> managerPrivileges = new HashSet<>();
        for (Privilege privilege : allPrivileges) {
            if (privilege.getName().contains("VIEW") || 
                privilege.getName().contains("CREATE") ||
                privilege.getName().contains("EDIT")) {
                managerPrivileges.add(privilege);
            }
        }
        
        Role storeManager = Role.builder()
                .name("STORE_MANAGER")
                .description("Store manager with view, create, and edit privileges")
                .privileges(managerPrivileges)
                .build();
        
        // Create Store Staff role with basic privileges
        Set<Privilege> staffPrivileges = new HashSet<>();
        for (Privilege privilege : allPrivileges) {
            if (privilege.getName().contains("VIEW") && !privilege.getName().contains("ALL")) {
                staffPrivileges.add(privilege);
            }
            if (privilege.getName().equals("ORDER_CREATE")) {
                staffPrivileges.add(privilege);
            }
        }
        
        Role storeStaff = Role.builder()
                .name("STORE_STAFF")
                .description("Store staff with basic view privileges")
                .privileges(staffPrivileges)
                .build();
        
        roleRepository.saveAll(Arrays.asList(superAdmin, storeManager, storeStaff));
    }

    private void initUsers() {
        // Get roles
        Role superAdminRole = roleRepository.findByName("SUPER_ADMIN")
                .orElseThrow(() -> new RuntimeException("SUPER_ADMIN role not found"));
        Role managerRole = roleRepository.findByName("STORE_MANAGER")
                .orElseThrow(() -> new RuntimeException("STORE_MANAGER role not found"));
        
        // Get tenants
        Tenant brandTenant = tenantRepository.findAll().stream()
                .filter(t -> t.getType().equals("brand") && t.getParentTenant() == null)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Brand tenant not found"));
        
        List<Tenant> storeTenants = tenantRepository.findAll().stream()
                .filter(t -> t.getType().equals("store") && t.getParentTenant() != null)
                .toList();
        
        if (storeTenants.isEmpty()) {
            throw new RuntimeException("No store tenants found");
        }
        
        // Create admin user for brand
        User admin = User.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin123"))
                .email("admin@store-management.com")
                .tenant(brandTenant)
                .roles(Collections.singleton(superAdminRole))
                .enabled(true)
                .build();
        
        // Create manager user for first store
        User manager = User.builder()
                .username("manager")
                .password(passwordEncoder.encode("manager123"))
                .email("manager@store-management.com")
                .tenant(storeTenants.get(0))
                .roles(Collections.singleton(managerRole))
                .enabled(true)
                .build();
        
        userRepository.saveAll(Arrays.asList(admin, manager));
    }
}
