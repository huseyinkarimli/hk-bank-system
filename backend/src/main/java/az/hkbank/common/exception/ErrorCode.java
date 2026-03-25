package az.hkbank.common.exception;

import lombok.Getter;

/**
 * Enumeration of all error codes used in the HK Bank System.
 * Each error code is associated with an HTTP status code and a descriptive message.
 */
@Getter
public enum ErrorCode {

    USER_NOT_FOUND(404, "İstifadəçi tapılmadı"),
    USER_ALREADY_EXISTS(409, "Bu email artıq qeydiyyatdan keçib"),
    INVALID_CREDENTIALS(401, "Email və ya şifrə yanlışdır"),
    
    ACCOUNT_NOT_FOUND(404, "Hesab tapılmadı"),
    ACCOUNT_ALREADY_EXISTS(409, "Bu valyuta üçün hesab artıq mövcuddur"),
    ACCOUNT_BLOCKED(403, "Hesab bloklanıb"),
    ACCOUNT_CLOSED(403, "Hesab bağlanıb"),
    INSUFFICIENT_BALANCE(400, "Balans kifayət deyil"),
    UNAUTHORIZED_ACCOUNT_ACCESS(403, "Bu hesaba giriş icazəniz yoxdur"),
    
    CARD_NOT_FOUND(404, "Kart tapılmadı"),
    CARD_BLOCKED(403, "Kart bloklanıb"),
    CARD_FROZEN(403, "Kart dondurulub"),
    CARD_LIMIT_EXCEEDED(400, "Bu hesab üçün kart limiti aşılıb"),
    INVALID_PIN(401, "PIN kod yanlışdır"),
    UNAUTHORIZED_CARD_ACCESS(403, "Bu karta giriş icazəniz yoxdur"),
    
    TRANSACTION_FAILED(500, "Əməliyyat uğursuz oldu"),
    TRANSACTION_NOT_FOUND(404, "Əməliyyat tapılmadı"),
    INVALID_TRANSACTION_AMOUNT(400, "Əməliyyat məbləği yanlışdır"),
    SAME_ACCOUNT_TRANSFER(400, "Eyni hesaba köçürmə mümkün deyil"),
    DAILY_LIMIT_EXCEEDED(400, "Gündəlik limit aşıldı"),
    FRAUD_DETECTED(403, "Şübhəli fəaliyyət aşkar edildi"),
    
    CURRENCY_NOT_SUPPORTED(400, "Valyuta dəstəklənmir"),
    
    PAYMENT_NOT_FOUND(404, "Ödəniş tapılmadı"),
    PAYMENT_FAILED(500, "Ödəniş uğursuz oldu"),
    PAYMENT_LIMIT_EXCEEDED(400, "Ödəniş məbləği maksimum limiti aşır"),
    PROVIDER_REJECTED(400, "Ödəniş provayder tərəfindən rədd edildi"),
    
    NOTIFICATION_NOT_FOUND(404, "Bildiriş tapılmadı"),
    STATEMENT_GENERATION_FAILED(500, "Çıxarış yaradılarkən xəta baş verdi"),
    
    CHAT_SESSION_NOT_FOUND(404, "Söhbət sessiyası tapılmadı"),
    AI_SERVICE_UNAVAILABLE(503, "AI xidməti müvəqqəti əlçatan deyil"),
    
    UNAUTHORIZED(401, "İcazəsiz giriş"),
    FORBIDDEN(403, "Bu əməliyyat üçün icazəniz yoxdur"),
    VALIDATION_ERROR(400, "Daxil edilən məlumatlar yanlışdır"),
    INTERNAL_SERVER_ERROR(500, "Server xətası baş verdi");

    private final int httpStatus;
    private final String message;

    ErrorCode(int httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
