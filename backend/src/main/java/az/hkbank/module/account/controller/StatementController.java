package az.hkbank.module.account.controller;

import az.hkbank.common.response.ApiResponse;
import az.hkbank.module.account.dto.StatementData;
import az.hkbank.module.account.service.StatementService;
import az.hkbank.module.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * REST controller for account statement operations.
 * Handles statement retrieval and PDF generation endpoints.
 */
@RestController
@RequestMapping("/api/accounts/{accountId}/statement")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Statement Management", description = "Account statement generation and retrieval endpoints")
public class StatementController {

    private final StatementService statementService;

    /**
     * Retrieves statement data for an account.
     *
     * @param accountId the account ID
     * @param from the start date
     * @param to the end date
     * @param currentUser the authenticated user
     * @return ApiResponse containing statement data
     */
    @GetMapping
    @Operation(summary = "Get account statement", description = "Retrieves statement data for an account within a date range")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Statement retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Unauthorized access to account"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Account not found"
            )
    })
    public ResponseEntity<ApiResponse<StatementData>> getStatement(
            @PathVariable Long accountId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @AuthenticationPrincipal User currentUser) {
        StatementData statement = statementService.getStatement(accountId, currentUser.getId(), from, to);
        return ResponseEntity.ok(ApiResponse.success(statement));
    }

    /**
     * Downloads a PDF statement for an account.
     *
     * @param accountId the account ID
     * @param from the start date
     * @param to the end date
     * @param currentUser the authenticated user
     * @return PDF file as byte array
     */
    @GetMapping("/pdf")
    @Operation(summary = "Download PDF statement", description = "Generates and downloads a PDF statement for an account")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "PDF generated successfully",
                    content = @Content(mediaType = "application/pdf")
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Unauthorized access to account"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Account not found"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "Statement generation failed"
            )
    })
    public ResponseEntity<byte[]> downloadPdfStatement(
            @PathVariable Long accountId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @AuthenticationPrincipal User currentUser) {
        byte[] pdfBytes = statementService.generatePdfStatement(accountId, currentUser.getId(), from, to);

        String filename = "statement_" + accountId + "_" + 
                from.toLocalDate() + "_to_" + to.toLocalDate() + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}
