package ao.gov.sgcd.pm.dto;

import lombok.*;

@Data
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AuthResponseDTO {
    private String token;
    private String refreshToken;
    private String role;
    private Long expiresIn;
}
