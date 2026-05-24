package in.MoneyManager.Chavanni.controller;

import in.MoneyManager.Chavanni.dto.ExpenseDTO;
import in.MoneyManager.Chavanni.dto.FilterDTO;
import in.MoneyManager.Chavanni.dto.IncomeDTO;
import in.MoneyManager.Chavanni.service.ExpenseService;
import in.MoneyManager.Chavanni.service.IncomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/filter")
public class FilterConstructor {

    private final ExpenseService expenseService;
    private final IncomeService incomeService; // fix: was IncomeController

    @PostMapping
    public ResponseEntity<?> filterTransaction(@RequestBody FilterDTO filter) {

        // Apply defaults if null
        LocalDate startDate = filter.getStartDate() != null
                ? filter.getStartDate()
                : LocalDate.MIN;

        LocalDate endDate = filter.getEndDate() != null
                ? filter.getEndDate()
                : LocalDate.now();

        String keyword = filter.getKeyword() != null
                ? filter.getKeyword()
                : "";

        String sortField = filter.getSortField() != null
                ? filter.getSortField()
                : "date";

        Sort.Direction direction = "desc".equalsIgnoreCase(filter.getSortOrder())
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Sort sort = Sort.by(direction, sortField);

        // Set resolved values back into filter
        filter.setStartDate(startDate);
        filter.setEndDate(endDate);
        filter.setKeyword(keyword);
        filter.setSortField(sortField);
        filter.setSortOrder(direction.name());

        if ("income".equalsIgnoreCase(filter.getType())) {
            List<IncomeDTO> incomes = incomeService.filterIncomes(filter);
            return ResponseEntity.ok(incomes);
        } else if ("expense".equalsIgnoreCase(filter.getType())) {
            List<ExpenseDTO> expenses = expenseService.filterExpenses(filter);
            return ResponseEntity.ok(expenses);
        } else {
            return ResponseEntity.badRequest()
                    .body("Invalid type. Must be 'income' or 'expense'.");
        }
    }
}