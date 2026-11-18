package sahra.db;

import java.time.LocalDate;

/** Simple data holders that mirror rows of the database tables. */
public final class Models {

    private Models() { }

    /** A row of the user table (without the password). */
    public record User(int id, String fullName, String email) { }

    /** A row of friend_group. toString() is what a JList shows. */
    public record Group(int id, String name, int createdBy, int memberCount) {
        @Override public String toString() { return name; }
    }

    /** A user who belongs to a group. */
    public record Member(int id, String fullName, String email) {
        @Override public String toString() { return fullName + "  (" + email + ")"; }
    }

    /** A row of the activity_with_type view. */
    public record Activity(int id, String name, String type, String area, String address,
                           String atmosphere, double price, String phone) { }

    /** A row of event plus the name of its final activity (if decided). */
    public record Event(int id, int groupId, Integer finalActivityId, String finalName, int createdBy,
                        String title, LocalDate plannedDate, String status, String area, String type,
                        String atmosphere, double minBudget, double maxBudget, boolean fallback) { }

    /** A group member and the place they voted for (null = not voted yet). */
    public record Voter(String name, String choice) { }

    /** One proposed activity of an event, with how many votes it has. */
    public record ResultRow(int activityId, String name, String area, String atmosphere,
                            double price, String matchType, int rank, int votes) { }
}
