package util; // Define the package containing monetary helper utilities

public final class Money { // Provide a static utility class for parsing and formatting money values
    private Money() {} // Private constructor prevents instantiation because all members are static

    public static int parseCents(String s) { // Convert a textual amount into an integer number of cents
        if (s == null || s.isBlank()) return 0; // Treat null or blank input as zero value
        String t = s.replaceAll("[^0-9.]", ""); // Remove any non-numeric or non-decimal characters such as currency symbols
        if (t.isEmpty()) return 0; // Return zero if nothing remains after stripping characters
        double d = Double.parseDouble(t); // Parse the cleaned text as a floating-point number of dollars
        return (int) Math.round(d * 100.0); // Convert dollars to cents by multiplying and rounding to the nearest integer
    } // End parseCents method

    public static String formatNTD(int cents) { // Format an integer number of cents as an NT$ currency string
        return String.format("NT$%.2f", cents / 100.0); // Divide by 100 to get dollars and format with two decimal places
    } // End formatNTD method
} // End Money utility class definition
