package sys.com.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sys.com.model.User;
import sys.com.model.Role;
import sys.com.model.Permission;
import sys.com.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findByEmailAndEnabledTrue(String email) {
        return userRepository.findByEmailAndEnabledTrue(email);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public User registerUser(String nombre, String apellido, String email, String rawPassword, Role role) {
        if (existsByEmail(email)) {
            throw new RuntimeException("El email ya está en uso");
        }

        User user = new User();
        user.setNombre(nombre);
        user.setApellido(apellido);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(role);

        if (role == Role.USER) {
            user.getPermissions().add(Permission.READ);
            user.getPermissions().add(Permission.CREATE);
        } else if (role == Role.ADMIN) {
            user.getPermissions().addAll(Set.of(Permission.values()));
        }

        return save(user);
    }

    public User updateUser(Long id, String nombre, String apellido, String email, Set<Permission> permissions) {
        User user = findById(id).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!user.getEmail().equals(email) && existsByEmail(email)) {
            throw new RuntimeException("El email ya está en uso por otro usuario");
        }
        
        user.setNombre(nombre);
        user.setApellido(apellido);
        user.setEmail(email);
        user.setPermissions(permissions);
        
        return save(user);
    }

    public void updatePassword(Long id, String newPassword) {
        User user = findById(id).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        user.setPassword(passwordEncoder.encode(newPassword));
        save(user);
    }

    public void toggleEnabled(Long id) {
        User user = findById(id).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        user.setEnabled(!user.isEnabled());
        save(user);
    }

    public void deleteById(Long id) {
        User user = findById(id).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        if (user.isAdmin()) {
            long adminCount = userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.ADMIN && u.isEnabled())
                .count();
            if (adminCount <= 1) {
                throw new RuntimeException("No se puede eliminar el último administrador activo");
            }
        }
        userRepository.deleteById(id);
    }

    public boolean validatePassword(User user, String rawPassword) {
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }
}
