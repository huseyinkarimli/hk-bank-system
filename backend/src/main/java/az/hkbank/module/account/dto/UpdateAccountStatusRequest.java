package az.hkbank.module.account.dto;

import az.hkbank.module.account.entity.AccountStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for account status update requests (admin only).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAccountStatusRequest {

    @NotNull(message = "Status is required")
    private AccountStatus status;
}
