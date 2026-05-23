package in.MoneyManager.Chavanni.controller;

import in.MoneyManager.Chavanni.dto.ExpenseDTO;
import in.MoneyManager.Chavanni.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping
    public ResponseEntity<ExpenseDTO> saveExpense(@RequestBody ExpenseDTO expenseDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(expenseService.saveExpense(expenseDTO));
    }

    @GetMapping
    public ResponseEntity<List<ExpenseDTO>> getExpenses(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        return ResponseEntity.ok(expenseService.getExpensesByDateRange(startDate, endDate));
    }

    @GetMapping("/search")
    public ResponseEntity<List<ExpenseDTO>> searchExpenses(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "date") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        return ResponseEntity.ok(expenseService.searchExpenses(startDate, endDate, keyword, sort));
    }

    @GetMapping("/recent")
    public ResponseEntity<List<ExpenseDTO>> getRecentExpenses() {
        return ResponseEntity.ok(expenseService.getTop5RecentExpenses());
    }

    @GetMapping("/total")
    public ResponseEntity<BigDecimal> getTotalExpenses() {
        return ResponseEntity.ok(expenseService.getTotalExpenses());
    }

    @PutMapping("/{expenseId}")
    public ResponseEntity<ExpenseDTO> updateExpense(@PathVariable Long expenseId,
                                                    @RequestBody ExpenseDTO expenseDTO) {
        return ResponseEntity.ok(expenseService.updateExpense(expenseId, expenseDTO));
    }

    @DeleteMapping("/{expenseId}")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long expenseId) {
        expenseService.deleteExpense(expenseId);
        return ResponseEntity.noContent().build();
    }
}