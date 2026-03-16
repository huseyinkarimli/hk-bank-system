package az.hkbank.module.admin.dto;

import az.hkbank.module.account.entity.AccountStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for changing an account's status.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangeAccountStatusRequest {

    @NotNull(message = "Status is required")
    private AccountStatus status;

    private String reason;
}
