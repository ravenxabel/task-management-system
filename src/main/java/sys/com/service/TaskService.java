package sys.com.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import sys.com.dto.TaskDto;
import sys.com.model.Task;
import sys.com.model.User;
import sys.com.model.Role;
import sys.com.repository.TaskRepository;
import sys.com.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    public Task save(Task task) {

        if (task.getId() == null) {
            User currentUser = getCurrentUser();
            if (currentUser != null) {
                task.setCreatedBy(currentUser);
            }
        }
        return taskRepository.save(task);
    }

    public List<Task> getTasksForCurrentUser() {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return List.of();
        }

        if (currentUser.isAdmin()) {
            return taskRepository.findAll();
        }

        List<Task> userTasks = taskRepository.findByCreatedByOrderByCreatedDesc(currentUser);

        if (userTasks.isEmpty()) {
            List<Task> orphanTasks = taskRepository.findAll().stream()
                .filter(task -> task.getCreatedBy() == null)
                .toList();
            if (!orphanTasks.isEmpty()) {
                orphanTasks.forEach(task -> {
                    task.setCreatedBy(currentUser);
                    taskRepository.save(task);
                });
                return orphanTasks;
            }
        }

        return userTasks;
    }

    public List<Task> getAll() {

        return taskRepository.findAll();
    }

    public Task getById(Long id) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return null;
        }

        Optional<Task> task = taskRepository.findById(id);
        if (task.isEmpty()) {
            return null;
        }

        Task foundTask = task.get();

        if (currentUser.isAdmin()) {
            return foundTask;
        }

        if (foundTask.getCreatedBy().getId().equals(currentUser.getId())) {
            return foundTask;
        }

        return null;
    }

    public void delete(Long id) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("Usuario no autenticado");
        }

        Task task = getById(id);
        if (task == null) {
            throw new RuntimeException("Tarea no encontrada o sin permisos");
        }

        taskRepository.deleteById(id);
    }

    public void toggleStatus(Long id) {
        Task task = getById(id);
        if (task == null) {
            throw new RuntimeException("Tarea no encontrada o sin permisos");
        }

        String newStatus = "Completada".equals(task.getStatus()) ? "Pendiente" : "Completada";
        task.setStatus(newStatus);
        taskRepository.save(task);
    }

    public long countTasksByStatus(String status) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return 0;
        }

        if (currentUser.isAdmin()) {
            return taskRepository.findAll().stream()
                .filter(task -> status.equals(task.getStatus()))
                .count();
        }

        return taskRepository.countByCreatedByAndStatus(currentUser, status);
    }

    public long countAllTasksForCurrentUser() {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return 0;
        }

        if (currentUser.isAdmin()) {
            return taskRepository.count();
        }

        return taskRepository.countByCreatedBy(currentUser);
    }

    public List<TaskDto> getTasksAsDto() {
        List<Task> tasks = getTasksForCurrentUser();
        return tasks.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private TaskDto convertToDto(Task task) {
        String createdByName = "";
        String createdByEmail = "";

        if (task.getCreatedBy() != null) {
            createdByName = task.getCreatedBy().getFullName();
            createdByEmail = task.getCreatedBy().getEmail();
        }

        return new TaskDto(
            task.getId(),
            task.getTitle(),
            task.getDescription(),
            task.getDueDate(),
            task.getPriority(),
            task.getStatus(),
            task.getCreated(),
            createdByName,
            createdByEmail
        );
    }

    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            String email = ((UserDetails) principal).getUsername();
            return userRepository.findByEmailAndEnabledTrue(email).orElse(null);
        }
        return null;
    }
}
