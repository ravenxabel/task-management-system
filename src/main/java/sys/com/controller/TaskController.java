package sys.com.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import sys.com.model.Task;
import sys.com.model.User;
import sys.com.service.TaskService;
import sys.com.service.UserService;

import java.util.Map;

@Controller
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private UserService userService;

    @GetMapping("")
    @PreAuthorize("hasAuthority('PERMISSION_READ')")
    public String listTasks(Model model) {
        User currentUser = getCurrentUser();
        model.addAttribute("tasks", taskService.getTasksForCurrentUser());
        model.addAttribute("currentUser", currentUser);
        return "tasks";
    }

    @GetMapping("/new")
    @PreAuthorize("hasAuthority('PERMISSION_CREATE')")
    public String showAddForm(Model model) {
        model.addAttribute("task", new Task());
        return "task_form";
    }

    @PostMapping("")
    @PreAuthorize("hasAuthority('PERMISSION_CREATE')")
    public String saveTaskFromForm(@ModelAttribute Task task, RedirectAttributes redirectAttributes) {
        try {
            taskService.save(task);
            redirectAttributes.addFlashAttribute("successMessage", "Tarea creada exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al crear la tarea: " + e.getMessage());
        }
        return "redirect:/tasks";
    }

    @PostMapping("/save")
    @PreAuthorize("hasAuthority('PERMISSION_CREATE') or hasAuthority('PERMISSION_UPDATE')")
    public String saveTaskFromModal(@ModelAttribute Task task, RedirectAttributes redirectAttributes,
                                   @RequestHeader(value = "referer", required = false) String referer) {
        try {
            taskService.save(task);
            redirectAttributes.addFlashAttribute("successMessage",
                task.getId() == null ? "Tarea creada exitosamente" : "Tarea actualizada exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                "Error al guardar la tarea: " + e.getMessage());
        }

        if (referer != null && referer.contains("/tasks")) {
            return "redirect:/tasks";
        }
        return "redirect:/home";
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('PERMISSION_UPDATE')")
    @ResponseBody
    public ResponseEntity<?> editTask(@PathVariable Long id) {
        try {
            Task task = taskService.getById(id);

            if (task == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(Map.of(
                    "id", task.getId(),
                    "title", task.getTitle(),
                    "description", task.getDescription() != null ? task.getDescription() : "",
                    "priority", task.getPriority() != null ? task.getPriority() : "",
                    "status", task.getStatus(),
                    "dueDate", task.getDueDate() != null ? task.getDueDate().toString() : ""
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "No tienes permisos para ver esta tarea"));
        }
    }

    @PostMapping("/toggle/{id}")
    @PreAuthorize("hasAuthority('PERMISSION_UPDATE')")
    public String toggleStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            taskService.toggleStatus(id);
            redirectAttributes.addFlashAttribute("successMessage", "Estado de la tarea actualizado");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al cambiar estado: " + e.getMessage());
        }
        return "redirect:/home";
    }

    @PostMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('PERMISSION_DELETE')")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes,
                        @RequestHeader(value = "referer", required = false) String referer) {
        try {
            taskService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Tarea eliminada exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al eliminar tarea: " + e.getMessage());
        }

        if (referer != null && referer.contains("/tasks")) {
            return "redirect:/tasks";
        }
        return "redirect:/home";
    }

    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            String email = ((UserDetails) principal).getUsername();
            return userService.findByEmailAndEnabledTrue(email).orElse(null);
        }
        return null;
    }
}


