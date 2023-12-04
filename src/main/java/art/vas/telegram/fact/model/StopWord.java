package art.vas.telegram.fact.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Entity
public class StopWord {
    @Id
    Long id;

    @NotBlank
    String word;
}
