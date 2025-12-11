package sys.com.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import sys.com.dto.UserEditDto;
import sys.com.model.Permission;
import sys.com.model.Role;
import sys.com.model.User;
import sys.com.service.UserService;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private UserService userService;

    @GetMapping("/users")
    public String listUsers(Model model) {
        List<User> users = userService.findAll();
        model.addAttribute("users", users);
        return "admin/users";
    }

    @GetMapping("/users/edit/{id}")
    public String showEditUserForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        User user = userService.findById(id).orElse(null);
        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Usuario no encontrado");
            return "redirect:/admin/users";
        }

        UserEditDto editDto = new UserEditDto(
            user.getId(),
            user.getNombre(),
            user.getApellido(),
            user.getEmail(),
            user.getPermissions()
        );

        model.addAttribute("user", editDto);
        model.addAttribute("allPermissions", Arrays.asList(Permission.values()));
        return "admin/edit-user";
    }

    @PostMapping("/users/edit")
    public String updateUser(@Valid @ModelAttribute("user") UserEditDto editDto,
                            BindingResult bindingResult,
                            RedirectAttributes redirectAttributes,
                            Model model,
                            HttpServletRequest request) {

        Set<Permission> permissions = new HashSet<>();
        for (Permission permission : Permission.values()) {
            String paramName = "permission_" + permission.name();
            if ("true".equals(request.getParameter(paramName))) {
                permissions.add(permission);
            }
        }
        editDto.setPermissions(permissions);


        if (!editDto.isPasswordMatching()) {
            bindingResult.rejectValue("confirmPassword", "error.user",
                "Las contraseñas no coinciden");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("allPermissions", Arrays.asList(Permission.values()));
            return "admin/edit-user";
        }

        try {
            userService.updateUser(
                editDto.getId(),
                editDto.getNombre(),
                editDto.getApellido(),
                editDto.getEmail(),
                editDto.getPermissions()
            );

            if (editDto.isPasswordChangeRequested()) {
                userService.updatePassword(editDto.getId(), editDto.getNewPassword());
            }

            redirectAttributes.addFlashAttribute("successMessage",
                "Usuario actualizado exitosamente");
            return "redirect:/admin/users";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                "Error al actualizar usuario: " + e.getMessage());
            return "redirect:/admin/users/edit/" + editDto.getId();
        }
    }

    @PostMapping("/users/toggle-enabled/{id}")
    public String toggleUserEnabled(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.toggleEnabled(id);
            redirectAttributes.addFlashAttribute("successMessage",
                "Estado del usuario actualizado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                "Error al cambiar estado del usuario: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage",
                "Usuario eliminado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                "Error al eliminar usuario: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/users/create")
    public String showCreateUserForm(Model model) {
        model.addAttribute("user", new UserEditDto());
        model.addAttribute("allPermissions", Arrays.asList(Permission.values()));
        model.addAttribute("roles", Arrays.asList(Role.values()));
        return "admin/create-user";
    }

    @PostMapping("/users/create")
    public String createUser(@Valid @ModelAttribute("user") UserEditDto editDto,
                            @RequestParam("role") Role role,
                            BindingResult bindingResult,
                            RedirectAttributes redirectAttributes,
                            Model model) {

        if (!editDto.isPasswordMatching() || !editDto.isPasswordChangeRequested()) {
            bindingResult.rejectValue("newPassword", "error.user",
                "Debe proporcionar una contraseña válida");
        }

        if (userService.existsByEmail(editDto.getEmail())) {
            bindingResult.rejectValue("email", "error.user",
                "Ya existe un usuario con este email");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("allPermissions", Arrays.asList(Permission.values()));
            model.addAttribute("roles", Arrays.asList(Role.values()));
            return "admin/create-user";
        }

        try {
            User newUser = userService.registerUser(
                editDto.getNombre(),
                editDto.getApellido(),
                editDto.getEmail(),
                editDto.getNewPassword(),
                role
            );

            if (!editDto.getPermissions().isEmpty()) {
                userService.updateUser(
                    newUser.getId(),
                    newUser.getNombre(),
                    newUser.getApellido(),
                    newUser.getEmail(),
                    editDto.getPermissions()
                );
            }

            redirectAttributes.addFlashAttribute("successMessage",
                "Usuario creado exitosamente");
            return "redirect:/admin/users";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                "Error al crear usuario: " + e.getMessage());
            model.addAttribute("allPermissions", Arrays.asList(Permission.values()));
            model.addAttribute("roles", Arrays.asList(Role.values()));
            return "admin/create-user";
        }
    }
}
