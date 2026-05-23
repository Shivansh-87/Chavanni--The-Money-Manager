package in.MoneyManager.Chavanni.controller;

import in.MoneyManager.Chavanni.dto.IncomeDTO;
import in.MoneyManager.Chavanni.service.IncomeService;
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
@RequestMapping("/incomes")
public class IncomeController {

    private final IncomeService incomeService;

    @PostMapping
    public ResponseEntity<IncomeDTO> saveIncome(@RequestBody IncomeDTO incomeDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(incomeService.saveIncome(incomeDTO));
    }

    @GetMapping
    public ResponseEntity<List<IncomeDTO>> getIncomes(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        return ResponseEntity.ok(incomeService.getIncomesByDateRange(startDate, endDate));
    }

    @GetMapping("/search")
    public ResponseEntity<List<IncomeDTO>> searchIncomes(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "date") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        return ResponseEntity.ok(incomeService.searchIncomes(startDate, endDate, keyword, sort));
    }

    @GetMapping("/recent")
    public ResponseEntity<List<IncomeDTO>> getRecentIncomes() {
        return ResponseEntity.ok(incomeService.getTop5RecentIncomes());
    }

    @GetMapping("/total")
    public ResponseEntity<BigDecimal> getTotalIncomes() {
        return ResponseEntity.ok(incomeService.getTotalIncomes());
    }

    @PutMapping("/{incomeId}")
    public ResponseEntity<IncomeDTO> updateIncome(@PathVariable Long incomeId,
                                                  @RequestBody IncomeDTO incomeDTO) {
        return ResponseEntity.ok(incomeService.updateIncome(incomeId, incomeDTO));
    }

    @DeleteMapping("/{incomeId}")
    public ResponseEntity<Void> deleteIncome(@PathVariable Long incomeId) {
        incomeService.deleteIncome(incomeId);
        return ResponseEntity.noContent().build();
    }
}