package art.vas.telegram.fact.model.repo;

import art.vas.telegram.fact.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface UserRepo extends JpaRepository<Users, Long>, PagingAndSortingRepository<Users, Long> {
}
