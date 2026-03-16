package az.hkbank.module.payment.dto;

import az.hkbank.module.payment.entity.ProviderType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for available providers list.
 * Contains provider type and associated provider names.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProviderListResponse {

    private ProviderType providerType;
    private List<String> providers;
}
