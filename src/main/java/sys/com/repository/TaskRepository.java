package sys.com.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sys.com.model.Task;
import sys.com.model.User;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByCreatedByOrderByCreatedDesc(User createdBy);

    List<Task> findByCreatedByAndStatusOrderByCreatedDesc(User createdBy, String status);

    long countByCreatedBy(User createdBy);

    long countByCreatedByAndStatus(User createdBy, String status);
}
