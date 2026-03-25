package az.hkbank.common.util;

import az.hkbank.module.account.entity.CurrencyType;

import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * Utility class for generating IBAN and account numbers.
 * Generates unique identifiers for bank accounts following Azerbaijan banking standards.
 */
public final class IbanGenerator {

    private static final String COUNTRY_CODE = "AZ";
    private static final String BANK_CODE = "HKBA";
    private static final SecureRandom RANDOM = new SecureRandom();

    private IbanGenerator() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Generates a unique IBAN for the given currency type.
     * Format: AZ + 2 check digits + "HKBA" + 16 digit account number
     * Example: AZ21HKBA00000000001234567890
     *
     * @param currencyType the currency type
     * @return generated IBAN
     */
    public static String generateIban(CurrencyType currencyType) {
        String accountNumber = generateAccountNumber(currencyType);
        String paddedAccountNumber = String.format("%016d", Long.parseLong(accountNumber));
        
        String bban = BANK_CODE + paddedAccountNumber;
        String checkDigits = calculateCheckDigits(bban);
        
        return COUNTRY_CODE + checkDigits + bban;
    }

    /**
     * Generates a unique 10-digit account number.
     * First digit is currency prefix:
     * - AZN → starts with "1"
     * - USD → starts with "2"
     * - EUR → starts with "3"
     *
     * @param currencyType the currency type
     * @return generated account number
     */
    public static String generateAccountNumber(CurrencyType currencyType) {
        String prefix = getCurrencyPrefix(currencyType);
        
        long randomPart = 100000000L + RANDOM.nextLong(900000000L);
        
        return prefix + randomPart;
    }

    /**
     * Calculates check digits using MOD-97 algorithm.
     *
     * @param bban the Basic Bank Account Number
     * @return two-digit check number as string
     */
    private static String calculateCheckDigits(String bban) {
        String rearranged = bban + COUNTRY_CODE + "00";
        
        String numericString = convertToNumeric(rearranged);
        
        BigInteger numericValue = new BigInteger(numericString);
        int remainder = numericValue.mod(BigInteger.valueOf(97)).intValue();
        int checkDigits = 98 - remainder;
        
        return String.format("%02d", checkDigits);
    }

    /**
     * Converts alphabetic characters to numeric values for MOD-97 calculation.
     * A=10, B=11, ..., Z=35
     *
     * @param input the input string
     * @return numeric representation
     */
    private static String convertToNumeric(String input) {
        StringBuilder numeric = new StringBuilder();
        for (char c : input.toCharArray()) {
            if (Character.isDigit(c)) {
                numeric.append(c);
            } else if (Character.isLetter(c)) {
                numeric.append(Character.toUpperCase(c) - 'A' + 10);
            }
        }
        return numeric.toString();
    }

    /**
     * Gets the currency prefix for account number generation.
     *
     * @param currencyType the currency type
     * @return currency prefix
     */
    private static String getCurrencyPrefix(CurrencyType currencyType) {
        return switch (currencyType) {
            case AZN -> "1";
            case USD -> "2";
            case EUR -> "3";
        };
    }

    /**
     * Validates an IBAN using MOD-97 algorithm.
     *
     * @param iban the IBAN to validate
     * @return true if valid, false otherwise
     */
    public static boolean validateIban(String iban) {
        if (iban == null || iban.length() < 15) {
            return false;
        }
        
        String rearranged = iban.substring(4) + iban.substring(0, 4);
        String numericString = convertToNumeric(rearranged);
        
        BigInteger numericValue = new BigInteger(numericString);
        return numericValue.mod(BigInteger.valueOf(97)).intValue() == 1;
    }
}
