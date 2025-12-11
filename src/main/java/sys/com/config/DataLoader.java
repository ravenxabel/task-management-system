package sys.com.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import sys.com.model.Role;
import sys.com.model.Permission;
import sys.com.service.UserService;

import java.util.Set;

@Component
public class DataLoader implements ApplicationRunner {

    @Autowired
    private UserService userService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        System.out.println("Iniciando carga de datos por defecto...");

        if (!userService.existsByEmail("admin@medisupply.com")) {
            try {
                userService.registerUser(
                    "Administrador",
                    "del Sistema",
                    "admin@medisupply.com",
                    "admin123",
                    Role.ADMIN
                );
                System.out.println("✅ Usuario administrador creado:");
                System.out.println("   Email: admin@medisupply.com");
                System.out.println("   Password: admin123");
            } catch (Exception e) {
                System.err.println("❌ Error al crear usuario administrador: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("ℹ️  Usuario administrador ya existe");
        }

        if (!userService.existsByEmail("user@medisupply.com")) {
            try {
                userService.registerUser(
                    "Usuario",
                    "de Prueba",
                    "user@medisupply.com",
                    "user123",
                    Role.USER
                );
                System.out.println("✅ Usuario de prueba creado:");
                System.out.println("   Email: user@medisupply.com");
                System.out.println("   Password: user123");
            } catch (Exception e) {
                System.err.println("❌ Error al crear usuario de prueba: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("ℹ️  Usuario de prueba ya existe");
        }

        System.out.println("✨ Carga de datos completada");
    }
}
