-- V3: Budget tracking tables
-- Project-level budget configuration and per-sprint cost tracking

CREATE TABLE project_budget (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    hourly_rate DECIMAL(8,2) NOT NULL DEFAULT 85.00,
    total_budget DECIMAL(12,2) NOT NULL DEFAULT 57800.00,
    currency VARCHAR(3) NOT NULL DEFAULT 'EUR',
    contingency_percent DECIMAL(5,2) NOT NULL DEFAULT 10.00,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE sprint_budget (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sprint_id BIGINT NOT NULL,
    planned_cost DECIMAL(12,2) NOT NULL,
    actual_cost DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    notes TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_sprint_budget_sprint FOREIGN KEY (sprint_id) REFERENCES sprints(id),
    CONSTRAINT uq_sprint_budget_sprint UNIQUE (sprint_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Seed project budget (204 sessions × ~3.33h avg × €85/h ≈ €57,800)
INSERT INTO project_budget (hourly_rate, total_budget, currency, contingency_percent) VALUES (85.00, 57800.00, 'EUR', 10.00);

-- Seed sprint budgets (sprint.total_hours × €85)
INSERT INTO sprint_budget (sprint_id, planned_cost) VALUES
    (1, (SELECT total_hours * 85.00 FROM sprints WHERE sprint_number = 1)),
    (2, (SELECT total_hours * 85.00 FROM sprints WHERE sprint_number = 2)),
    (3, (SELECT total_hours * 85.00 FROM sprints WHERE sprint_number = 3)),
    (4, (SELECT total_hours * 85.00 FROM sprints WHERE sprint_number = 4)),
    (5, (SELECT total_hours * 85.00 FROM sprints WHERE sprint_number = 5)),
    (6, (SELECT total_hours * 85.00 FROM sprints WHERE sprint_number = 6));
