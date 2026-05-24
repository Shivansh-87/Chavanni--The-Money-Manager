package in.MoneyManager.Chavanni.service;

import in.MoneyManager.Chavanni.dto.ExpenseDTO;
import in.MoneyManager.Chavanni.dto.IncomeDTO;
import in.MoneyManager.Chavanni.dto.RecentTransactionDTO;
import in.MoneyManager.Chavanni.entity.ProfileEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class DashBoardService {

    private final IncomeService incomeService;
    private final ExpenseService expenseService;
    private final ProfileService profileService;

    public Map<String, Object> getDashboardData() {
        ProfileEntity profile = profileService.getCurrentProfile();

        // Get latest 5 incomes and expenses
        List<IncomeDTO> latestIncomes = incomeService.getTop5RecentIncomes();
        List<ExpenseDTO> latestExpenses = expenseService.getTop5RecentExpenses();

        // Get totals
        BigDecimal totalIncome = incomeService.getTotalIncomes();
        BigDecimal totalExpense = expenseService.getTotalExpenses();
        BigDecimal balance = totalIncome.subtract(totalExpense);

        // Merge incomes and expenses into one sorted list
        List<RecentTransactionDTO> recentTransactions = Stream.concat(
                        latestIncomes.stream().map(income -> RecentTransactionDTO.builder()
                                .id(income.getId())
                                .name(income.getName())
                                .icon(income.getIcon())
                                .categoryName(income.getCategoryName())
                                .amount(income.getAmount())
                                .date(income.getDate())
                                .createdAt(income.getCreatedAt())   // add this
                                .updatedAt(income.getUpdatedAt())
                                .type("INCOME")
                                .build()),
                        latestExpenses.stream().map(expense -> RecentTransactionDTO.builder()
                                .id(expense.getId())
                                .name(expense.getName())
                                .icon(expense.getIcon())
                                .categoryName(expense.getCategoryName())
                                .amount(expense.getAmount())
                                .date(expense.getDate())
                                .createdAt(expense.getCreatedAt())   // add this
                                .updatedAt(expense.getUpdatedAt())
                                .type("EXPENSE")
                                .build())
                )
                .sorted(Comparator.comparing(RecentTransactionDTO::getDate).reversed())
                .limit(5)
                .toList();

        Map<String, Object> returnValue = new LinkedHashMap<>();
        returnValue.put("totalIncome", totalIncome);
        returnValue.put("totalExpense", totalExpense);
        returnValue.put("balance", balance);
        returnValue.put("recentTransactions", recentTransactions);

        return returnValue;
    }
}