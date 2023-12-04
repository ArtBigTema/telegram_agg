package art.vas.telegram.fact.model.repo;

import art.vas.telegram.fact.model.Fact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface FactRepo extends JpaRepository<Fact, Long>, PagingAndSortingRepository<Fact, Long> {
}
