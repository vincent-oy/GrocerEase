package util;

/**
 * Money helper functions.
 * We store money in "cents" (integer) to avoid rounding problems that happen with doubles.
 * Example: "12.34" dollars => 1234 cents
 */
public final class Money {

    private Money() { }

    /**
     * Turn a human string like "123.45" or "NT$123.45" into an integer number of cents.
     * If the string is empty or invalid, we default to 0.
     */
    public static int parseCents(String text) {

        if (text == null) {
            return 0;
        }

        // Keep only digits and the decimal point.
        String cleaned = text.replaceAll("[^0-9.]", "");

        if (cleaned.isEmpty()) {
            return 0;
        }

        double value = Double.parseDouble(cleaned);

        // Convert to cents by multiplying by 100 and rounding to nearest int.
        return (int) Math.round(value * 100.0);
    }

    /**
     * Format cents back into a display string like "NT$12.34".
     */
    public static String formatNTD(int cents) {
        return String.format("NT$%.2f", cents / 100.0);
    }
}
