package backend;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseTest {

    Connection connection;

    @BeforeEach
    void setUp() throws SQLException {
        Database.createTestConnection();
        connection = Database.getConnection();
    }

    @AfterEach
    void tearDown() throws SQLException {
        connection.close();
        File f = new File("test_database.db");
        f.delete();
    }

    @Test
    void insertClient() throws SQLException {
        Database.insertClient("abc", "def");
        Database.commitData();
        ResultSet rs = Database.query("SELECT * FROM clients");
        assertTrue(rs.next());
        assertTrue(rs.next());
        assertEquals("abc", rs.getString(2));
        assertEquals("def", rs.getString(3));
    }

    @Test
    void insertCampaign() throws SQLException {
        Database.insertCampaign("abc", 1);
        Database.commitData();
        ResultSet rs = Database.query("SELECT * FROM campaigns");
        assertTrue(rs.next());
        assertTrue(rs.next());
        assertEquals("abc", rs.getString(2));
    }

    @Test
    void insertImpressionLog() throws SQLException {
        Database.insertImpressionLog("2015-01-01 12:00:05", Common.Context.BLOG, 123L, 0.2f, 1);
        Database.commitData();
        ResultSet rs = Database.query("SELECT * FROM impression_log");
        assertTrue(rs.next());
        assertEquals("2015-01-01 12:00:05", rs.getString(2));
        assertEquals("BLOG", rs.getString(3));
        assertEquals(123L, rs.getLong(4));
        assertEquals(0.2f, rs.getFloat(5));
        assertEquals(1, rs.getInt(6));
    }

    @Test
    void insertUser() throws SQLException {
        Database.insertUser(123L, Common.Gender.FEMALE, Common.AgeRange.F25T34, Common.Income.LOW, 1);
        Database.commitData();
        ResultSet rs = Database.query("SELECT * FROM users");
        assertTrue(rs.next());
        assertEquals(123L, rs.getLong(1));
        assertEquals("FEMALE", rs.getString(2));
        assertEquals("F25T34", rs.getString(3));
        assertEquals("LOW", rs.getString(4));
        assertEquals(1, rs.getInt(5));
    }

    @Test
    void insertServerLog() throws SQLException {
        Database.insertServerLog("2015-01-01 12:00:05", "2015-01-01 12:00:05", 5, true, 123L, 1);
        Database.insertServerLog("2015-01-01 12:00:05", null, 5, true, 123L, 1);
        Database.commitData();
        ResultSet rs = Database.query("SELECT * FROM server_log");
        assertTrue(rs.next());
        assertEquals("2015-01-01 12:00:05.0", rs.getString(2));
        assertEquals("2015-01-01 12:00:05.0", rs.getString(3));
        assertEquals(0, rs.getInt(4));
        assertEquals(5, rs.getInt(5));
        assertTrue(rs.getBoolean(6));
        assertEquals(123L, rs.getInt(7));
        assertEquals(1, rs.getInt(8));
        assertTrue(rs.next());
        assertNull(rs.getString(3));
    }

    @Test
    void insertClickLog() throws SQLException {
        Database.insertClickLog("2015-01-01 12:00:05", 0.2f, 123L, 1);
        Database.commitData();
        ResultSet rs = Database.query("SELECT * FROM click_log");
        assertTrue(rs.next());
        assertEquals("2015-01-01 12:00:05", rs.getString(2));
        assertEquals(0.2f, rs.getFloat(3));
        assertEquals(123L, rs.getLong(4));
        assertEquals(1, rs.getInt(5));
    }

    @Test
    void query1() throws SQLException {
        Database.insertClickLog("2015-01-01 12:00:05", 0.2f, 123L, 1);
        Database.insertClickLog("2015-01-01 12:00:05", 0.2f, 123L, 1);
        Database.insertClickLog("2015-01-01 12:00:05", 0.2f, 123L, 2);
        Database.commitData();
        PreparedStatement statement = Database.getConnection().prepareStatement("SELECT * FROM click_log WHERE campaign_id = ?");
        statement.setInt(1, 1);
        ResultSet rs = Database.query(statement);
        assertTrue(rs.next());
        assertTrue(rs.next());
        assertFalse(rs.next());
    }
}