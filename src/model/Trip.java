package model; // Define the package containing trip data models

public class Trip { // Represent a shopping trip persisted in the database
    public Integer id; // Primary key assigned by the database or null until inserted
    public String tripDateText; // Trip date stored as a simple text string for easier binding in the UI
    public Integer storeId; // Optional foreign key referencing a store (unused in current UI but available for extensions)
    public int budgetCents; // Planned budget for the trip stored in cents to avoid floating point issues
    public String note; // Optional free-form note about the trip

    @Override // Indicate we are overriding the default toString implementation
    public String toString() { // Return a readable representation summarizing key trip details
        return "Trip{id=" + id + ", date=" + tripDateText + ", budgetCents=" + budgetCents + "}"; // Compose a concise description using ID, date, and budget
    } // End toString override
} // End Trip class definition
