package az.hkbank.common.util;

import java.util.Random;

/**
 * Utility class for generating and managing card numbers.
 * Generates valid card numbers following Luhn algorithm and provides masking functionality.
 */
public final class CardNumberGenerator {

    private static final String BIN = "44222007";
    private static final Random RANDOM = new Random();

    private CardNumberGenerator() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Generates a unique 16-digit card number.
     * Format: "4422 2007 XXXX XXXX" (stored without spaces)
     * Validates using Luhn algorithm.
     *
     * @return generated card number
     */
    public static String generateCardNumber() {
        StringBuilder cardNumber = new StringBuilder(BIN);
        
        for (int i = 0; i < 7; i++) {
            cardNumber.append(RANDOM.nextInt(10));
        }
        
        int checkDigit = calculateLuhnCheckDigit(cardNumber.toString());
        cardNumber.append(checkDigit);
        
        return cardNumber.toString();
    }

    /**
     * Generates a 3-digit CVV.
     *
     * @return generated CVV as plain text
     */
    public static String generateCvv() {
        return String.format("%03d", RANDOM.nextInt(1000));
    }

    /**
     * Generates a 4-digit PIN.
     *
     * @return generated PIN as plain text
     */
    public static String generatePin() {
        return String.format("%04d", RANDOM.nextInt(10000));
    }

    /**
     * Masks a card number for display.
     * Input:  "4422200712345678"
     * Output: "4422 **** **** 5678"
     *
     * @param cardNumber the card number to mask
     * @return masked card number
     */
    public static String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() != 16) {
            return cardNumber;
        }
        
        String first4 = cardNumber.substring(0, 4);
        String last4 = cardNumber.substring(12, 16);
        
        return first4 + " **** **** " + last4;
    }

    /**
     * Calculates the Luhn check digit for a card number.
     *
     * @param partialCardNumber the card number without check digit
     * @return the check digit
     */
    private static int calculateLuhnCheckDigit(String partialCardNumber) {
        int sum = 0;
        boolean alternate = true;
        
        for (int i = partialCardNumber.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(partialCardNumber.charAt(i));
            
            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            
            sum += digit;
            alternate = !alternate;
        }
        
        return (10 - (sum % 10)) % 10;
    }

    /**
     * Validates a card number using the Luhn algorithm.
     *
     * @param cardNumber the card number to validate
     * @return true if valid, false otherwise
     */
    public static boolean validateCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() != 16) {
            return false;
        }
        
        int sum = 0;
        boolean alternate = false;
        
        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(cardNumber.charAt(i));
            
            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            
            sum += digit;
            alternate = !alternate;
        }
        
        return (sum % 10) == 0;
    }

    /**
     * Formats a card number with spaces for display.
     * Input:  "4422200712345678"
     * Output: "4422 2007 1234 5678"
     *
     * @param cardNumber the card number to format
     * @return formatted card number
     */
    public static String formatCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() != 16) {
            return cardNumber;
        }
        
        return cardNumber.substring(0, 4) + " " +
               cardNumber.substring(4, 8) + " " +
               cardNumber.substring(8, 12) + " " +
               cardNumber.substring(12, 16);
    }
}
