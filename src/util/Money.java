package util;

/**
 * Money utilities.
 * We store amounts in cents (integers) to avoid floating-point rounding issues.
 */
public final class Money {

    private Money() { }

    /** Convert "NT$123.45" or "123.45" to 12345 cents. Blank => 0. */
    public static int parseCents(String text) {
        if (text == null) return 0;
        String t = text.replaceAll("[^0-9.]", "");
        if (t.isEmpty()) return 0;
        double d = Double.parseDouble(t);
        return (int) Math.round(d * 100.0);
    }

    /** Format cents to NT$ string like "NT$123.45". */
    public static String formatNTD(int cents) {
        return String.format("NT$%.2f", cents / 100.0);
    }
}
