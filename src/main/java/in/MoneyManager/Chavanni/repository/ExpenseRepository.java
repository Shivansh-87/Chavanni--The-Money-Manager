package in.MoneyManager.Chavanni.repository;

import in.MoneyManager.Chavanni.entity.ExpenseEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public class ExpenseRepository extends JpaRepository<ExpenseEntity, Long> {

    List<ExpenseEntity> findByProfileId(Long profileId);
}
