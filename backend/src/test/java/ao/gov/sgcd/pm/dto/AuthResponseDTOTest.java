package ao.gov.sgcd.pm.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthResponseDTOTest {

    @Test
    void builder_shouldCreateDTOWithAllFields() {
        AuthResponseDTO dto = AuthResponseDTO.builder()
                .token("eyJhbGciOiJIUzI1NiJ9.test.signature")
                .role("DEVELOPER")
                .expiresIn(86400L)
                .build();

        assertEquals("eyJhbGciOiJIUzI1NiJ9.test.signature", dto.getToken());
        assertEquals("DEVELOPER", dto.getRole());
        assertEquals(86400L, dto.getExpiresIn());
    }

    @Test
    void noArgConstructor_shouldCreateEmptyDTO() {
        AuthResponseDTO dto = new AuthResponseDTO();

        assertNull(dto.getToken());
        assertNull(dto.getRole());
        assertNull(dto.getExpiresIn());
    }

    @Test
    void allArgsConstructor_shouldSetAllFields() {
        AuthResponseDTO dto = new AuthResponseDTO(
                "jwt-token-value", "refresh-token-value", "STAKEHOLDER", 3600L
        );

        assertEquals("jwt-token-value", dto.getToken());
        assertEquals("refresh-token-value", dto.getRefreshToken());
        assertEquals("STAKEHOLDER", dto.getRole());
        assertEquals(3600L, dto.getExpiresIn());
    }

    @Test
    void gettersAndSetters_shouldWorkForAllFields() {
        AuthResponseDTO dto = new AuthResponseDTO();

        dto.setToken("new-token");
        dto.setRole("DEVELOPER");
        dto.setExpiresIn(7200L);

        assertEquals("new-token", dto.getToken());
        assertEquals("DEVELOPER", dto.getRole());
        assertEquals(7200L, dto.getExpiresIn());
    }

    @Test
    void equals_reflexive() {
        AuthResponseDTO dto = AuthResponseDTO.builder().token("token1").role("DEVELOPER").build();
        assertEquals(dto, dto);
    }

    @Test
    void equals_symmetric() {
        AuthResponseDTO dto1 = AuthResponseDTO.builder().token("token1").role("DEVELOPER").expiresIn(86400L).build();
        AuthResponseDTO dto2 = AuthResponseDTO.builder().token("token1").role("DEVELOPER").expiresIn(86400L).build();

        assertEquals(dto1, dto2);
        assertEquals(dto2, dto1);
    }

    @Test
    void equals_nullReturnsFalse() {
        AuthResponseDTO dto = AuthResponseDTO.builder().token("token1").build();
        assertNotEquals(null, dto);
    }

    @Test
    void equals_differentClassReturnsFalse() {
        AuthResponseDTO dto = AuthResponseDTO.builder().token("token1").build();
        assertNotEquals("a string", dto);
    }

    @Test
    void equals_differentValuesReturnsFalse() {
        AuthResponseDTO dto1 = AuthResponseDTO.builder().token("token1").role("DEVELOPER").build();
        AuthResponseDTO dto2 = AuthResponseDTO.builder().token("token2").role("STAKEHOLDER").build();

        assertNotEquals(dto1, dto2);
    }

    @Test
    void equals_differentTokenReturnsFalse() {
        AuthResponseDTO dto1 = AuthResponseDTO.builder().token("token1").role("DEVELOPER").expiresIn(86400L).build();
        AuthResponseDTO dto2 = AuthResponseDTO.builder().token("token2").role("DEVELOPER").expiresIn(86400L).build();

        assertNotEquals(dto1, dto2);
    }

    @Test
    void equals_differentRoleReturnsFalse() {
        AuthResponseDTO dto1 = AuthResponseDTO.builder().token("token1").role("DEVELOPER").build();
        AuthResponseDTO dto2 = AuthResponseDTO.builder().token("token1").role("STAKEHOLDER").build();

        assertNotEquals(dto1, dto2);
    }

    @Test
    void hashCode_equalObjectsSameHashCode() {
        AuthResponseDTO dto1 = AuthResponseDTO.builder().token("token1").role("DEVELOPER").expiresIn(86400L).build();
        AuthResponseDTO dto2 = AuthResponseDTO.builder().token("token1").role("DEVELOPER").expiresIn(86400L).build();

        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void hashCode_differentObjectsDifferentHashCode() {
        AuthResponseDTO dto1 = AuthResponseDTO.builder().token("token1").role("DEVELOPER").build();
        AuthResponseDTO dto2 = AuthResponseDTO.builder().token("token2").role("STAKEHOLDER").build();

        assertNotEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void toString_containsClassNameAndFieldValues() {
        AuthResponseDTO dto = AuthResponseDTO.builder()
                .token("jwt-token")
                .role("DEVELOPER")
                .expiresIn(86400L)
                .build();

        String result = dto.toString();
        assertTrue(result.contains("AuthResponseDTO"));
        assertTrue(result.contains("jwt-token"));
        assertTrue(result.contains("DEVELOPER"));
        assertTrue(result.contains("86400"));
    }
}
