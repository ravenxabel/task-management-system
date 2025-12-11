package sys.com.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import sys.com.model.User;
import sys.com.service.TaskService;
import sys.com.service.UserService;

@Controller
public class HomeController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private UserService userService;

    @GetMapping("/home")
    public String home(Model model) {

        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return "redirect:/";
        }

        var tasks = taskService.getTasksForCurrentUser();

        var tasksDto = taskService.getTasksAsDto();

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        String tasksJson = "[]";
        try {
            tasksJson = mapper.writeValueAsString(tasksDto);
        } catch (JsonProcessingException e) {
            System.err.println("Error al serializar tareas a JSON: " + e.getMessage());
            e.printStackTrace();
        }

        long totalTasks = taskService.countAllTasksForCurrentUser();
        long pendingTasks = taskService.countTasksByStatus("Pendiente");
        long completedTasks = taskService.countTasksByStatus("Completada");
        long overdueTasks = taskService.countTasksByStatus("Vencida");

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("tasks", tasks);
        model.addAttribute("tasksJson", tasksJson);
        model.addAttribute("totalTasks", totalTasks);
        model.addAttribute("pendingTasks", pendingTasks);
        model.addAttribute("completedTasks", completedTasks);
        model.addAttribute("overdueTasks", overdueTasks);

        return "home";
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


