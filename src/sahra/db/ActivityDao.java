package sahra.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import sahra.db.Models.Activity;

/** Everything the app does with activity and its 8 sub-type tables. */
public final class ActivityDao {

    private ActivityDao() { }

    public static final String[] TYPES = {
            "Restaurant", "Cinema", "Party", "Concert", "Karting", "Paintball", "Bowling", "Escape Room"};
    public static final String[] AREAS = {"Beirut", "Jounieh", "Byblos", "Batroun"};
    public static final String[] ATMOSPHERES = {"Chill", "Lively", "Fancy"};

    /** Kind of value stored in a sub-type column (decides which input box the form shows). */
    public enum Kind { TEXT, INT, BOOL, DATE, TIME }

    /** One sub-type column: its name in the table, the label shown on screen, and its kind. */
    public record Field(String column, String label, Kind kind) { }

    /** One sub-type: its table name and its own columns. */
    public record SubType(String table, Field... fields) { }

    /** The 8 sub-types from the LDM, keyed by the type name used in the app. */
    public static final Map<String, SubType> SUBTYPES = new LinkedHashMap<>();
    static {
        SUBTYPES.put("Restaurant", new SubType("restaurant",
                new Field("cuisineType", "Cuisine", Kind.TEXT),
                new Field("hasOutdoorSeating", "Outdoor seating", Kind.BOOL),
                new Field("reservationRequired", "Reservation required", Kind.BOOL)));
        SUBTYPES.put("Cinema", new SubType("cinema",
                new Field("timeAvailability", "Opening hours (e.g. 14:00 - 23:30)", Kind.TEXT),
                new Field("filmsShowing", "Films showing", Kind.TEXT),
                new Field("roomType", "Room type (Standard/IMAX/VIP/4DX)", Kind.TEXT),
                new Field("seatsAvailability", "Seats available", Kind.INT)));
        SUBTYPES.put("Party", new SubType("party",
                new Field("eventDate", "Date (YYYY-MM-DD)", Kind.DATE),
                new Field("musicGenre", "Music genre", Kind.TEXT),
                new Field("minAge", "Minimum age", Kind.INT)));
        SUBTYPES.put("Concert", new SubType("concert",
                new Field("artistPerforming", "Artist", Kind.TEXT),
                new Field("musicGenre", "Music genre", Kind.TEXT),
                new Field("eventDate", "Date (YYYY-MM-DD)", Kind.DATE),
                new Field("showStartTime", "Start time (HH:MM)", Kind.TIME),
                new Field("ticketSaleStartDate", "Ticket sales start (YYYY-MM-DD)", Kind.DATE)));
        SUBTYPES.put("Karting", new SubType("karting",
                new Field("isReservationRequired", "Reservation required", Kind.BOOL),
                new Field("minAge", "Minimum age", Kind.INT)));
        SUBTYPES.put("Paintball", new SubType("paintball",
                new Field("environmentType", "Environment (Indoor/Outdoor/Both)", Kind.TEXT),
                new Field("numberOfFields", "Number of fields", Kind.INT),
                new Field("maxPlayers", "Max players", Kind.INT),
                new Field("isReservationRequired", "Reservation required", Kind.BOOL)));
        SUBTYPES.put("Bowling", new SubType("bowling",
                new Field("offersFoodService", "Food service", Kind.BOOL),
                new Field("shoeRentalIncluded", "Shoe rental included", Kind.BOOL)));
        SUBTYPES.put("Escape Room", new SubType("escape_room",
                new Field("themesAvailable", "Themes", Kind.TEXT),
                new Field("bookingRequired", "Booking required", Kind.BOOL),
                new Field("lobbyCapacity", "Lobby capacity", Kind.INT),
                new Field("difficultyLevel", "Difficulty (Easy/Medium/Hard/Expert)", Kind.TEXT)));
    }

    /**
     * Searches activities. Any filter that is null (or "All") is ignored.
     * Builds the WHERE clause from the filters that were chosen.
     */
    public static List<Activity> search(String type, String area, String atmosphere,
                                        Double minPrice, Double maxPrice, String nameContains) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT * FROM activity_with_type WHERE 1 = 1");
        List<Object> params = new ArrayList<>();
        if (type != null)       { sql.append(" AND activityType = ?"); params.add(type); }
        if (area != null)       { sql.append(" AND area = ?");         params.add(area); }
        if (atmosphere != null) { sql.append(" AND atmosphere = ?");   params.add(atmosphere); }
        if (minPrice != null)   { sql.append(" AND averagePrice >= ?"); params.add(minPrice); }
        if (maxPrice != null)   { sql.append(" AND averagePrice <= ?"); params.add(maxPrice); }
        if (nameContains != null && !nameContains.isBlank()) {
            sql.append(" AND name LIKE ?");
            params.add("%" + nameContains.trim() + "%");
        }
        sql.append(" ORDER BY activityType, name");

        List<Activity> list = new ArrayList<>();
        try (Connection c = Database.connect(); PreparedStatement ps = c.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(read(rs));
            }
        }
        return list;
    }

    public static Activity get(int activityId) throws SQLException {
        try (Connection c = Database.connect();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM activity_with_type WHERE activityID = ?")) {
            ps.setInt(1, activityId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? read(rs) : null;
            }
        }
    }

    private static Activity read(ResultSet rs) throws SQLException {
        return new Activity(rs.getInt("activityID"), rs.getString("name"), rs.getString("activityType"),
                rs.getString("area"), rs.getString("address"), rs.getString("atmosphere"),
                rs.getDouble("averagePrice"), rs.getString("phoneNb"));
    }

    /** Values of the sub-type columns for one activity (column name -> value). */
    public static Map<String, Object> subtypeValues(int activityId, String type) throws SQLException {
        SubType st = SUBTYPES.get(type);
        Map<String, Object> values = new LinkedHashMap<>();
        if (st == null) return values;
        try (Connection c = Database.connect();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM " + st.table() + " WHERE activityID = ?")) {
            ps.setInt(1, activityId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    for (Field f : st.fields()) {
                        values.put(f.column(), f.kind() == Kind.BOOL ? rs.getBoolean(f.column()) : rs.getObject(f.column()));
                    }
                }
            }
        }
        return values;
    }

    /** Turns what the user typed into the right Java value, or explains what is wrong. */
    public static Object parse(Field f, Object raw) {
        if (f.kind() == Kind.BOOL) return Boolean.TRUE.equals(raw);
        String text = raw == null ? "" : raw.toString().trim();
        if (text.isEmpty()) throw new IllegalArgumentException(f.label() + " cannot be empty.");
        try {
            return switch (f.kind()) {
                case INT -> Integer.parseInt(text);
                case DATE -> LocalDate.parse(text);
                case TIME -> LocalTime.parse(text.length() == 5 ? text : text.substring(0, Math.min(8, text.length())));
                default -> text;
            };
        } catch (NumberFormatException | DateTimeParseException e) {
            throw new IllegalArgumentException(f.label() + ": \"" + text + "\" is not valid.");
        }
    }

    /** Inserts the activity row and its sub-type row in one transaction. Returns the new id. */
    public static int insert(Activity a, Map<String, Object> sub) throws SQLException {
        SubType st = SUBTYPES.get(a.type());
        try (Connection c = Database.connect()) {
            c.setAutoCommit(false);
            try {
                int id;
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO activity (name, area, address, atmosphere, averagePrice, phoneNb) VALUES (?, ?, ?, ?, ?, ?)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    fillCommon(ps, a);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        keys.next();
                        id = keys.getInt(1);
                    }
                }
                StringBuilder cols = new StringBuilder("activityID");
                StringBuilder marks = new StringBuilder("?");
                for (Field f : st.fields()) {
                    cols.append(", ").append(f.column());
                    marks.append(", ?");
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO " + st.table() + " (" + cols + ") VALUES (" + marks + ")")) {
                    ps.setInt(1, id);
                    int i = 2;
                    for (Field f : st.fields()) ps.setObject(i++, sub.get(f.column()));
                    ps.executeUpdate();
                }
                c.commit();
                return id;
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        }
    }

    /** Updates the activity row and its sub-type row in one transaction. */
    public static void update(Activity a, Map<String, Object> sub) throws SQLException {
        SubType st = SUBTYPES.get(a.type());
        try (Connection c = Database.connect()) {
            c.setAutoCommit(false);
            try {
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE activity SET name = ?, area = ?, address = ?, atmosphere = ?, averagePrice = ?, phoneNb = ? "
                                + "WHERE activityID = ?")) {
                    fillCommon(ps, a);
                    ps.setInt(7, a.id());
                    ps.executeUpdate();
                }
                StringBuilder set = new StringBuilder();
                for (Field f : st.fields()) {
                    if (set.length() > 0) set.append(", ");
                    set.append(f.column()).append(" = ?");
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE " + st.table() + " SET " + set + " WHERE activityID = ?")) {
                    int i = 1;
                    for (Field f : st.fields()) ps.setObject(i++, sub.get(f.column()));
                    ps.setInt(i, a.id());
                    ps.executeUpdate();
                }
                c.commit();
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        }
    }

    /** Deletes an activity (its sub-type row goes with it through ON DELETE CASCADE). */
    public static void delete(int activityId) throws SQLException {
        try (Connection c = Database.connect();
             PreparedStatement ps = c.prepareStatement("DELETE FROM activity WHERE activityID = ?")) {
            ps.setInt(1, activityId);
            ps.executeUpdate();
        }
    }

    private static void fillCommon(PreparedStatement ps, Activity a) throws SQLException {
        ps.setString(1, a.name());
        ps.setString(2, a.area());
        ps.setString(3, a.address());
        ps.setString(4, a.atmosphere());
        ps.setDouble(5, a.price());
        ps.setString(6, a.phone() == null || a.phone().isBlank() ? null : a.phone());
    }
}
