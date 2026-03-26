package az.hkbank.module.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body for banning a user (soft delete, block cards, invalidate JWT session semantics).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BanUserRequest {

    @NotBlank(message = "Reason is required")
    private String reason;
}
