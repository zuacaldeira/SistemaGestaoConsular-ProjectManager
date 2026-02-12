package ao.gov.sgcd.pm.controller;

import ao.gov.sgcd.pm.dto.StakeholderDashboardDTO;
import ao.gov.sgcd.pm.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/stakeholder")
@RequiredArgsConstructor
@Tag(name = "Stakeholder", description = "Acesso ao dashboard do stakeholder com token de autenticação")
public class StakeholderController {

    private final DashboardService dashboardService;

    @Value("${sgcd-pm.stakeholder.token}")
    private String validToken;

    @Operation(summary = "Obter dashboard do stakeholder", description = "Devolve o dashboard executivo do stakeholder, acessível via token de autenticação")
    @ApiResponse(responseCode = "200", description = "Dashboard do stakeholder devolvido com sucesso")
    @ApiResponse(responseCode = "403", description = "Token de acesso inválido")
    @GetMapping
    public ResponseEntity<StakeholderDashboardDTO> getDashboard(
            @RequestParam(required = false) String token) {
        if (token == null || !token.equals(validToken)) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(dashboardService.getStakeholderDashboard());
    }
}
