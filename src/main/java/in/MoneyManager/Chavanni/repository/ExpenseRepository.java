package in.MoneyManager.Chavanni.repository;

import in.MoneyManager.Chavanni.entity.ExpenseEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public class ExpenseRepository extends JpaRepository<ExpenseEntity, Long> {
}
