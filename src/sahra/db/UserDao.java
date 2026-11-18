package sahra.db;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import sahra.db.Models.User;

/** Everything the app does with the user table. */
public final class UserDao {

    private UserDao() { }

    /** Returns the user if email + password are correct, otherwise null. */
    public static User login(String email, String password) throws SQLException {
        String sql = "SELECT userID, fullName, email FROM user WHERE email = ? AND password = ?";
        try (Connection c = Database.connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email.trim());
            ps.setString(2, hash(password));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? new User(rs.getInt(1), rs.getString(2), rs.getString(3)) : null;
            }
        }
    }

    /** Creates a new account. Fails with a duplicate-key error if the email is taken. */
    public static User register(String fullName, String email, String password) throws SQLException {
        String sql = "INSERT INTO user (fullName, email, password) VALUES (?, ?, ?)";
        try (Connection c = Database.connect();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, fullName.trim());
            ps.setString(2, email.trim());
            ps.setString(3, hash(password));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return new User(keys.getInt(1), fullName.trim(), email.trim());
            }
        }
    }

    /** SHA-256 hash as 64 hex characters, so plain passwords are never stored. */
    static String hash(String password) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : digest) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
