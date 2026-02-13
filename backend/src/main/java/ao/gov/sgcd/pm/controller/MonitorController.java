package ao.gov.sgcd.pm.controller;

import ao.gov.sgcd.pm.dto.MonitorDTO;
import ao.gov.sgcd.pm.service.MonitorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/monitor")
@RequiredArgsConstructor
@Tag(name = "Monitor", description = "Monitorização de serviços do sistema")
public class MonitorController {

    private final MonitorService monitorService;

    @GetMapping("/health")
    @Operation(summary = "Estado de saúde dos serviços", description = "Retorna o estado de todos os serviços monitorizados")
    @ApiResponse(responseCode = "200", description = "Estado retornado com sucesso")
    @ApiResponse(responseCode = "401", description = "Não autenticado")
    public ResponseEntity<MonitorDTO> getHealth() {
        return ResponseEntity.ok(monitorService.getHealth());
    }
}
