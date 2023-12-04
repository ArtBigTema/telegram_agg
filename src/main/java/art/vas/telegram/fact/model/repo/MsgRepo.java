package art.vas.telegram.fact.model.repo;

import art.vas.telegram.fact.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface MsgRepo extends JpaRepository<Message, Long>, PagingAndSortingRepository<Message, Long> {
}
