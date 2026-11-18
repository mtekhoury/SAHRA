package sahra.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import sahra.db.Models.Group;
import sahra.db.Models.Member;

/** Everything the app does with friend_group and group_member. */
public final class GroupDao {

    private GroupDao() { }

    /** Groups the user belongs to. */
    public static List<Group> listForUser(int userId) throws SQLException {
        String sql = """
                SELECT g.groupID, g.groupName, g.createdBy,
                       (SELECT COUNT(*) FROM group_member x WHERE x.groupID = g.groupID) AS memberCount
                FROM friend_group g
                JOIN group_member gm ON gm.groupID = g.groupID
                WHERE gm.userID = ?
                ORDER BY g.groupName""";
        List<Group> list = new ArrayList<>();
        try (Connection c = Database.connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(new Group(rs.getInt(1), rs.getString(2), rs.getInt(3), rs.getInt(4)));
            }
        }
        return list;
    }

    /** Creates a group and adds its creator as the first member (one transaction). */
    public static void create(String name, int creatorId) throws SQLException {
        try (Connection c = Database.connect()) {
            c.setAutoCommit(false);
            try (PreparedStatement g = c.prepareStatement(
                         "INSERT INTO friend_group (groupName, createdBy) VALUES (?, ?)",
                         Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement m = c.prepareStatement(
                         "INSERT INTO group_member (groupID, userID) VALUES (?, ?)")) {
                g.setString(1, name.trim());
                g.setInt(2, creatorId);
                g.executeUpdate();
                int groupId;
                try (ResultSet keys = g.getGeneratedKeys()) {
                    keys.next();
                    groupId = keys.getInt(1);
                }
                m.setInt(1, groupId);
                m.setInt(2, creatorId);
                m.executeUpdate();
                c.commit();
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        }
    }

    public static List<Member> members(int groupId) throws SQLException {
        String sql = """
                SELECT u.userID, u.fullName, u.email
                FROM group_member gm JOIN user u ON u.userID = gm.userID
                WHERE gm.groupID = ?
                ORDER BY u.fullName""";
        List<Member> list = new ArrayList<>();
        try (Connection c = Database.connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, groupId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(new Member(rs.getInt(1), rs.getString(2), rs.getString(3)));
            }
        }
        return list;
    }

    /** Adds the user with this email to the group. Returns their name, or null if no such user. */
    public static String addMemberByEmail(int groupId, String email) throws SQLException {
        try (Connection c = Database.connect();
             PreparedStatement find = c.prepareStatement("SELECT userID, fullName FROM user WHERE email = ?")) {
            find.setString(1, email.trim());
            try (ResultSet rs = find.executeQuery()) {
                if (!rs.next()) return null;
                int userId = rs.getInt(1);
                String name = rs.getString(2);
                try (PreparedStatement add = c.prepareStatement(
                        "INSERT INTO group_member (groupID, userID) VALUES (?, ?)")) {
                    add.setInt(1, groupId);
                    add.setInt(2, userId);
                    add.executeUpdate();
                }
                return name;
            }
        }
    }

    public static void leave(int groupId, int userId) throws SQLException {
        try (Connection c = Database.connect();
             PreparedStatement ps = c.prepareStatement("DELETE FROM group_member WHERE groupID = ? AND userID = ?")) {
            ps.setInt(1, groupId);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }
}
