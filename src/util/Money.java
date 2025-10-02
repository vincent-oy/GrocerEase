package util;

/*
 * simple helper for money (student style)
 * i just keep 2 static methods:
 * - parse string into cents
 * - format cents into NT$ string
 */
public final class Money {
    private Money() {}

    // parse "1200" or "1200.50" into cents (int)
    public static int parseCents(String s) {
        if (s == null || s.isBlank()) return 0;
        String t = s.replaceAll("[^0-9.]", ""); // strip NT$, commas, etc
        if (t.isEmpty()) return 0;
        double d = Double.parseDouble(t);
        return (int)Math.round(d * 100.0);
    }

    // format cents to "NT$X.XX"
    public static String formatNTD(int cents) {
        return String.format("NT$%.2f", cents / 100.0);
    }
}
