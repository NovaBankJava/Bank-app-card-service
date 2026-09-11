package org.example.bankappcardservice.domain.model;

public final class Luhn {

    private Luhn() {
    }

    public static boolean isValid(String number) {
        int sum = 0;
        boolean doubleDigit = false; // the rightmost digit is NOT doubled
        for (int i = number.length() - 1; i >= 0; i--) {
            int digit = number.charAt(i) - '0';
            if (doubleDigit) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            sum += digit;
            doubleDigit = !doubleDigit;
        }
        return sum % 10 == 0;
    }

    public static int checkDigit(String payload) {
        int sum = 0;
        boolean doubleDigit = true; // once appended, the last payload digit gets doubled
        for (int i = payload.length() - 1; i >= 0; i--) {
            int digit = payload.charAt(i) - '0';
            if (doubleDigit) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            sum += digit;
            doubleDigit = !doubleDigit;
        }
        return (10 - (sum % 10)) % 10;
    }
}