package util;

/*
    keep organise money 
    i just keep 2 static methods:
    - parse string into cents
    - format cents into NT$ string
 */
public final class Money {
    private Money() {}

    // parse "1200" or "1200.50" into cents(int)
    public static int parseCents(String s) {
        if (s == null || s.isBlank()) return 0; // non existent or empty --> 0
        String t = s.replaceAll("[^0-9.]", ""); // strip NT$, commas, etc; regex finds any character not a digit (0-9 or .), and the matched chars are replaced with nothing "", final value will be an int --> pure number
        if (t.isEmpty()) return 0;
        double d = Double.parseDouble(t);
        return (int)Math.round(d * 100.0);
    }

    // format cents to "NT$X.XX"
    public static String formatNTD(int cents) {
        return String.format("NT$%.2f", cents / 100.0);                         // takes an integer --> currency value (2 d.p.) --> done by /100
    }
}
