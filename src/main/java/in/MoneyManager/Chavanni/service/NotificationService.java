package in.MoneyManager.Chavanni.service;

import in.MoneyManager.Chavanni.dto.ExpenseDTO;
import in.MoneyManager.Chavanni.entity.ProfileEntity;
import in.MoneyManager.Chavanni.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final ProfileRepository profileRepository;
    private final EmailService emailService;
    private final ExpenseService expenseService;

    @Value("${money.manager.frontend.url}")
    private String frontendUrl;

    @Scheduled(cron = "0 0 22 * * *", zone = "Asia/Kolkata") // Every day at 10 PM
    // @Scheduled(cron = "0 * * * * *", zone = "Asia/Kolkata") // Every minute (for testing)
    public void sendDailyIncomeExpenseReminder() {
        log.info("Job started: sendDailyIncomeExpenseReminder()");
        List<ProfileEntity> profiles = profileRepository.findAll();
        for (ProfileEntity profile : profiles) {
            String body = "Hi " + profile.getFullName() + ",<br><br>"
                    + "This is a friendly reminder to add your income and expenses for today in Money Manager.<br><br>"
                    + "<a href='" + frontendUrl + "' style='display:inline-block;padding:10px 20px;"
                    + "background-color:#4CAF50;color:#fff;text-decoration:none;"
                    + "border-radius:5px;font-weight:bold;'>Go to Money Manager</a>"
                    + "<br><br>Best regards,<br>Team Chavanni";
            emailService.sendEmail(
                    profile.getEmail(),
                    "Daily Reminder: Add Your Income and Expenses",
                    body
            );
        }
        log.info("Job completed: sendDailyIncomeExpenseReminder()");
    }

    @Scheduled(cron = "0 0 23 * * *", zone = "Asia/Kolkata") // Every day at 11 PM
    // @Scheduled(cron = "0 * * * * *", zone = "Asia/Kolkata") // Every minute (for testing)
    public void sendDailyExpenseSummary() {
        log.info("Job started: sendDailyExpenseSummary()");
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Kolkata"));
        List<ProfileEntity> profiles = profileRepository.findAll();

        for (ProfileEntity profile : profiles) {
            // fix: pass only date, profileId comes from security context inside service
            List<ExpenseDTO> todaysExpenses = expenseService
                    .findByProfileIdAndDate(profile.getId(), today);

            if (!todaysExpenses.isEmpty()) {
                StringBuilder table = new StringBuilder();
                table.append("<table style='border-collapse:collapse;width:100%;'>");
                table.append("<tr style='background-color:#f2f2f2;'>")
                        .append("<th style='padding:8px;text-align:left;border:1px solid #ddd;'>#</th>")
                        .append("<th style='padding:8px;text-align:left;border:1px solid #ddd;'>Name</th>")
                        .append("<th style='padding:8px;text-align:right;border:1px solid #ddd;'>Amount</th>")
                        .append("<th style='padding:8px;text-align:left;border:1px solid #ddd;'>Category</th>")
                        .append("</tr>");

                int i = 1;
                for (ExpenseDTO expense : todaysExpenses) {
                    table.append("<tr>") // fix: was missing closing >
                            .append("<td style='border:1px solid #ddd;padding:8px;'>").append(i++).append("</td>")
                            .append("<td style='border:1px solid #ddd;padding:8px;'>").append(expense.getName()).append("</td>")
                            .append("<td style='border:1px solid #ddd;padding:8px;'>").append(expense.getAmount()).append("</td>")
                            .append("<td style='border:1px solid #ddd;padding:8px;'>")
                            .append(expense.getCategoryName() != null ? expense.getCategoryName() : "N/A")
                            .append("</td>")
                            .append("</tr>");
                }
                table.append("</table>"); // fix: was </tables>

                String body = "Hi " + profile.getFullName() + ",<br><br>" // fix: was missing space after Hi
                        + "Here is a summary of your expenses for today:<br><br>"
                        + table
                        + "<br><br>Best regards,<br>Team Chavanni";

                emailService.sendEmail(
                        profile.getEmail(),
                        "Daily Expense Summary",
                        body
                );
            }
        }
        log.info("Job completed: sendDailyExpenseSummary()");
    }
}