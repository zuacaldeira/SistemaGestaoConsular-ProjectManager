package ao.gov.sgcd.pm.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BudgetUpdateDTO {

    @NotNull
    @DecimalMin("1.00")
    private BigDecimal hourlyRate;

    @NotNull
    @DecimalMin("1.00")
    private BigDecimal totalBudget;

    @NotNull
    @Size(min = 3, max = 3)
    private String currency;

    @NotNull
    @DecimalMin("0.00")
    @DecimalMax("100.00")
    private BigDecimal contingencyPercent;
}
