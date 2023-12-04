package art.vas.telegram.fact.model.repo;

import art.vas.telegram.fact.model.StopWord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface StopWordRepo extends JpaRepository<StopWord, Long>, PagingAndSortingRepository<StopWord, Long> {
}
