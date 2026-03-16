package az.hkbank.module.admin.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for changing a user's active status.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangeUserStatusRequest {

    @NotNull(message = "Active status is required")
    private Boolean active;
}
