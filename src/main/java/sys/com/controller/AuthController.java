package sys.com.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import sys.com.dto.UserRegistrationDto;
import sys.com.model.Role;
import sys.com.service.UserService;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new UserRegistrationDto());
        return "register";
    }

    @PostMapping("/auth/register")
    public String registerUser(@Valid @ModelAttribute("user") UserRegistrationDto registrationDto,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes,
                              Model model) {

        if (!registrationDto.isPasswordMatching()) {
            bindingResult.rejectValue("confirmPassword", "error.user",
                "Las contraseñas no coinciden");
        }

        if (userService.existsByEmail(registrationDto.getEmail())) {
            bindingResult.rejectValue("email", "error.user",
                "Ya existe un usuario con este email");
        }

        if (bindingResult.hasErrors()) {
            return "register";
        }

        try {
            userService.registerUser(
                registrationDto.getNombre(),
                registrationDto.getApellido(),
                registrationDto.getEmail(),
                registrationDto.getPassword(),
                Role.USER
            );

            redirectAttributes.addFlashAttribute("successMessage",
                "Usuario registrado exitosamente. Ahora puedes iniciar sesión.");
            return "redirect:/?registered=true";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                "Error al registrar usuario: " + e.getMessage());
            return "redirect:/register?error=true";
        }
    }
}
