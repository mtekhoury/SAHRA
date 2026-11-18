package sahra.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import sahra.db.Models.Event;
import sahra.db.Models.ResultRow;
import sahra.db.Models.Voter;

/** Everything the app does with event, event_result and vote: search, fallback, voting, winner. */
public final class EventDao {

    private EventDao() { }

    /** Exact search: every filter must match and the price must be inside the budget. */
    private static final String EXACT_SEARCH = """
            SELECT activityID FROM activity_with_type
            WHERE activityType = ? AND area = ? AND atmosphere = ?
              AND averagePrice BETWEEN ? AND ?
            ORDER BY averagePrice ASC, name
            LIMIT 10""";

    /**
     * Fallback search (used only when the exact search finds nothing).
     * Keeps the activity type, then prefers the same area, then the same atmosphere,
     * then places inside the budget, then the price closest to the MINIMUM budget.
     */
    private static final String FALLBACK_SEARCH = """
            SELECT activityID FROM activity_with_type
            WHERE activityType = ?
            ORDER BY (area = ?) DESC,
                     (atmosphere = ?) DESC,
                     (averagePrice > ?) ASC,
                     ABS(averagePrice - ?) ASC,
                     averagePrice ASC
            LIMIT 5""";

    private static final String EVENT_SELECT = """
            SELECT e.*, a.name AS finalName
            FROM event e LEFT JOIN activity a ON a.activityID = e.finalActivityID
            """;

    public static List<Event> listForGroup(int groupId) throws SQLException {
        List<Event> list = new ArrayList<>();
        try (Connection c = Database.connect();
             PreparedStatement ps = c.prepareStatement(EVENT_SELECT + " WHERE e.groupID = ? ORDER BY e.plannedDate DESC")) {
            ps.setInt(1, groupId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(read(rs));
            }
        }
        return list;
    }

    public static Event get(int eventId) throws SQLException {
        try (Connection c = Database.connect();
             PreparedStatement ps = c.prepareStatement(EVENT_SELECT + " WHERE e.eventID = ?")) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? read(rs) : null;
            }
        }
    }

    private static Event read(ResultSet rs) throws SQLException {
        int finalId = rs.getInt("finalActivityID");
        Integer finalActivity = rs.wasNull() ? null : finalId;
        return new Event(rs.getInt("eventID"), rs.getInt("groupID"), finalActivity, rs.getString("finalName"),
                rs.getInt("createdBy"), rs.getString("title"), rs.getDate("plannedDate").toLocalDate(),
                rs.getString("status"), rs.getString("area"), rs.getString("activityType"),
                rs.getString("atmosphere"), rs.getDouble("minBudget"), rs.getDouble("maxBudget"),
                rs.getBoolean("isFallback"));
    }

    /**
     * Creates a SAHRA: saves the event with its filters, searches for matching activities
     * (falling back if nothing matches) and saves them as the event's results.
     * Everything happens in one transaction. Returns the new event id.
     */
    public static int create(int groupId, int userId, String title, LocalDate date, String type,
                             String area, String atmosphere, double minBudget, double maxBudget) throws SQLException {
        try (Connection c = Database.connect()) {
            c.setAutoCommit(false);
            try {
                int eventId;
                try (PreparedStatement ps = c.prepareStatement("""
                        INSERT INTO event (groupID, createdBy, title, plannedDate, status,
                                           area, activityType, atmosphere, minBudget, maxBudget, isFallback)
                        VALUES (?, ?, ?, ?, 'VOTING', ?, ?, ?, ?, ?, FALSE)""", Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, groupId);
                    ps.setInt(2, userId);
                    ps.setString(3, title.trim());
                    ps.setObject(4, date);
                    ps.setString(5, area);
                    ps.setString(6, type);
                    ps.setString(7, atmosphere);
                    ps.setDouble(8, minBudget);
                    ps.setDouble(9, maxBudget);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        keys.next();
                        eventId = keys.getInt(1);
                    }
                }

                // 1) exact search
                List<Integer> ids = new ArrayList<>();
                try (PreparedStatement ps = c.prepareStatement(EXACT_SEARCH)) {
                    ps.setString(1, type);
                    ps.setString(2, area);
                    ps.setString(3, atmosphere);
                    ps.setDouble(4, minBudget);
                    ps.setDouble(5, maxBudget);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) ids.add(rs.getInt(1));
                    }
                }

                // 2) nothing found -> fallback search based on the minimum budget
                String matchType = "EXACT";
                if (ids.isEmpty()) {
                    matchType = "FALLBACK";
                    try (PreparedStatement ps = c.prepareStatement(FALLBACK_SEARCH)) {
                        ps.setString(1, type);
                        ps.setString(2, area);
                        ps.setString(3, atmosphere);
                        ps.setDouble(4, maxBudget);
                        ps.setDouble(5, minBudget);
                        try (ResultSet rs = ps.executeQuery()) {
                            while (rs.next()) ids.add(rs.getInt(1));
                        }
                    }
                    try (PreparedStatement ps = c.prepareStatement("UPDATE event SET isFallback = TRUE WHERE eventID = ?")) {
                        ps.setInt(1, eventId);
                        ps.executeUpdate();
                    }
                }

                // 3) save the results in the order they will be shown
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO event_result (eventID, activityID, matchType, displayRank) VALUES (?, ?, ?, ?)")) {
                    int rank = 1;
                    for (int id : ids) {
                        ps.setInt(1, eventId);
                        ps.setInt(2, id);
                        ps.setString(3, matchType);
                        ps.setInt(4, rank++);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
                c.commit();
                return eventId;
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        }
    }

    /** The proposed activities of an event with their vote counts. */
    public static List<ResultRow> results(int eventId) throws SQLException {
        String sql = """
                SELECT er.activityID, a.name, a.area, a.atmosphere, a.averagePrice,
                       er.matchType, er.displayRank, COUNT(v.userID) AS votes
                FROM event_result er
                JOIN activity a  ON a.activityID = er.activityID
                LEFT JOIN vote v ON v.eventID = er.eventID AND v.activityID = er.activityID
                WHERE er.eventID = ?
                GROUP BY er.activityID, a.name, a.area, a.atmosphere, a.averagePrice, er.matchType, er.displayRank
                ORDER BY er.displayRank""";
        List<ResultRow> list = new ArrayList<>();
        try (Connection c = Database.connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new ResultRow(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4),
                            rs.getDouble(5), rs.getString(6), rs.getInt(7), rs.getInt(8)));
                }
            }
        }
        return list;
    }

    /** Which activity this user voted for in this event, or null. */
    public static Integer myVote(int eventId, int userId) throws SQLException {
        try (Connection c = Database.connect();
             PreparedStatement ps = c.prepareStatement("SELECT activityID FROM vote WHERE eventID = ? AND userID = ?")) {
            ps.setInt(1, eventId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : null;
            }
        }
    }

    /** Everyone in the group and what they voted for (choice is null if they have not voted). */
    public static List<Voter> voters(int eventId) throws SQLException {
        String sql = """
                SELECT u.fullName, a.name
                FROM event e
                JOIN group_member gm ON gm.groupID = e.groupID
                JOIN user u          ON u.userID   = gm.userID
                LEFT JOIN vote v     ON v.eventID  = e.eventID AND v.userID = u.userID
                LEFT JOIN activity a ON a.activityID = v.activityID
                WHERE e.eventID = ?
                ORDER BY u.fullName""";
        List<Voter> list = new ArrayList<>();
        try (Connection c = Database.connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(new Voter(rs.getString(1), rs.getString(2)));
            }
        }
        return list;
    }

    /** Casts or changes this user's vote (delete old vote + insert new one, in one transaction). */
    public static void vote(int eventId, int activityId, int userId) throws SQLException {
        try (Connection c = Database.connect()) {
            c.setAutoCommit(false);
            try {
                try (PreparedStatement check = c.prepareStatement(
                        "SELECT status FROM event WHERE eventID = ? FOR UPDATE")) {
                    check.setInt(1, eventId);
                    try (ResultSet rs = check.executeQuery()) {
                        if (!rs.next() || !"VOTING".equals(rs.getString(1))) {
                            throw new SQLException("Voting is closed for this SAHRA.");
                        }
                    }
                }
                try (PreparedStatement del = c.prepareStatement("DELETE FROM vote WHERE eventID = ? AND userID = ?")) {
                    del.setInt(1, eventId);
                    del.setInt(2, userId);
                    del.executeUpdate();
                }
                try (PreparedStatement ins = c.prepareStatement(
                        "INSERT INTO vote (eventID, activityID, userID) VALUES (?, ?, ?)")) {
                    ins.setInt(1, eventId);
                    ins.setInt(2, activityId);
                    ins.setInt(3, userId);
                    ins.executeUpdate();
                }
                c.commit();
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        }
    }

    /**
     * Closes voting: the activity with the most votes wins (a tie goes to the cheaper one).
     * Returns the winner's name. Throws if nobody has voted yet.
     */
    public static String close(int eventId) throws SQLException {
        String winnerSql = """
                SELECT er.activityID, a.name, COUNT(v.userID) AS votes
                FROM event_result er
                JOIN activity a  ON a.activityID = er.activityID
                LEFT JOIN vote v ON v.eventID = er.eventID AND v.activityID = er.activityID
                WHERE er.eventID = ?
                GROUP BY er.activityID, a.name, a.averagePrice, er.displayRank
                ORDER BY votes DESC, a.averagePrice ASC, er.displayRank ASC
                LIMIT 1""";
        try (Connection c = Database.connect()) {
            c.setAutoCommit(false);
            try {
                int winnerId;
                String winnerName;
                try (PreparedStatement ps = c.prepareStatement(winnerSql)) {
                    ps.setInt(1, eventId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next() || rs.getInt(3) == 0) {
                            throw new SQLException("Nobody has voted yet, so there is no winner.");
                        }
                        winnerId = rs.getInt(1);
                        winnerName = rs.getString(2);
                    }
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE event SET finalActivityID = ?, status = 'DECIDED' WHERE eventID = ?")) {
                    ps.setInt(1, winnerId);
                    ps.setInt(2, eventId);
                    ps.executeUpdate();
                }
                c.commit();
                return winnerName;
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        }
    }

    /** Deletes an event; its results and votes go with it (ON DELETE CASCADE). */
    public static void delete(int eventId) throws SQLException {
        try (Connection c = Database.connect();
             PreparedStatement ps = c.prepareStatement("DELETE FROM event WHERE eventID = ?")) {
            ps.setInt(1, eventId);
            ps.executeUpdate();
        }
    }
}
