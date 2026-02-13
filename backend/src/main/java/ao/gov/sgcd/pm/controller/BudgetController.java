package ao.gov.sgcd.pm.controller;

import ao.gov.sgcd.pm.dto.BudgetOverviewDTO;
import ao.gov.sgcd.pm.dto.BudgetUpdateDTO;
import ao.gov.sgcd.pm.service.BudgetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/budget")
@RequiredArgsConstructor
@Tag(name = "Budget", description = "Gestão de orçamento e custos do projecto")
public class BudgetController {

    private final BudgetService budgetService;

    @GetMapping
    @Operation(summary = "Resumo do orçamento", description = "Retorna visão geral do orçamento com custos por sprint")
    @ApiResponse(responseCode = "200", description = "Orçamento retornado com sucesso")
    @ApiResponse(responseCode = "401", description = "Não autenticado")
    public ResponseEntity<BudgetOverviewDTO> getBudgetOverview() {
        return ResponseEntity.ok(budgetService.getBudgetOverview());
    }

    @PutMapping
    @Operation(summary = "Actualizar orçamento", description = "Actualiza configuração do orçamento (apenas DEVELOPER)")
    @ApiResponse(responseCode = "200", description = "Orçamento actualizado com sucesso")
    @ApiResponse(responseCode = "401", description = "Não autenticado")
    @ApiResponse(responseCode = "403", description = "Sem permissão")
    public ResponseEntity<BudgetOverviewDTO> updateBudget(@Valid @RequestBody BudgetUpdateDTO dto) {
        return ResponseEntity.ok(budgetService.updateBudget(dto));
    }
}
