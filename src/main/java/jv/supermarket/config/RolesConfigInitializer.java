package jv.supermarket.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;

import jv.supermarket.user.Role;
import jv.supermarket.user.RoleRepository;
import jv.supermarket.user.User;
import jv.supermarket.user.UserService;

@Configuration
@Order(1)
@Profile("test")
public class RolesConfigInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    private final UserService userService;

    RolesConfigInitializer(RoleRepository roleRepository, UserService userService) {
        this.roleRepository = roleRepository;
        this.userService = userService;
    }

    @Override
    public void run(String... args) throws Exception {
        createRoleIfNotExists("ROLE_ADMIN");
        createRoleIfNotExists("ROLE_EMPLOYEE");
        createRoleIfNotExists("ROLE_CUSTOMER");

        createDefaultAdmin();
    }

    private void createRoleIfNotExists(String roleName) {
        if (!roleRepository.existsByName(roleName)) {
            Role role = new Role();
            role.setName(roleName);
            roleRepository.save(role);
            System.out.println("Role created: " + roleName);
        } else {
            System.out.println("Role already exists: " + roleName);
        }
    }

    private void createDefaultAdmin() {
        String email = "admin@gmail.com";

        if (!userService.existsByEmail(email)) {
            User admin = new User();
            admin.setName("Administrador");
            admin.setEmail(email);
            admin.setPassword("123456");

            userService.saveAdmin(admin);
            System.out.println("Default admin created: " + email);
        } else {
            System.out.println("Default admin already exists: " + email);
        }
    }
}
