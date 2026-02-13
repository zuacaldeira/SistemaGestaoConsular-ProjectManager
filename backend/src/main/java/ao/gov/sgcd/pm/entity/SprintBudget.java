package ao.gov.sgcd.pm.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "sprint_budget")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SprintBudget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sprint_id", nullable = false, unique = true)
    private Sprint sprint;

    @Column(name = "planned_cost", nullable = false, precision = 12, scale = 2)
    private BigDecimal plannedCost;

    @Column(name = "actual_cost", nullable = false, precision = 12, scale = 2)
    private BigDecimal actualCost;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (actualCost == null) actualCost = BigDecimal.ZERO;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
