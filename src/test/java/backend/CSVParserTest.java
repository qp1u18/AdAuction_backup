package backend;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class CSVParserTest {

    private final String impression_log = "test_data/impression_log_test.csv";
    private final String server_log = "test_data/server_log_test.csv";
    private final String click_log = "test_data/click_log_test.csv";

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
    @DisplayName("Test database is created")
    void databaseExists() {
        File f = new File("test_database.db");
        assertTrue(f.exists());
    }

    @Test
    @DisplayName("Parsing into DB")
    void parseCSV() throws SQLException, CSVParser.ParseError {
        CSVParser.parseCSV(impression_log, 0);
        CSVParser.parseCSV(click_log,0);
        CSVParser.parseCSV(server_log,0);

        assertEquals(Database.query("SELECT COUNT(*) FROM impression_log").getString(1), "20");
        assertEquals(Database.query("SELECT COUNT(*) FROM server_log").getString(1), "10");
        assertEquals(Database.query("SELECT COUNT(*) FROM click_log").getString(1), "10");
        assertEquals(Database.query("SELECT COUNT(*) FROM users").getString(1), "20");
    }

    @Test
    @DisplayName("Test INSERT negative")
    void negativeTest() {
        assertThrows(CSVParser.ParseError.class, () -> CSVParser.parseCSV("test_data/server_log_negative.csv", 0));
        assertThrows(CSVParser.ParseError.class, () -> CSVParser.parseCSV("test_data/impression_log_negative.csv", 0));
        assertThrows(CSVParser.ParseError.class, () -> CSVParser.parseCSV("test_data/click_log_negative.csv", 0));
    }

    @Test
    @DisplayName("Unique entries")
    void uniqueEntries() throws SQLException, CSVParser.ParseError {
        CSVParser.parseCSV(impression_log, 0);
        assertEquals(Database.query("SELECT COUNT(DISTINCT user_id) FROM impression_log").getString(1), "20");
    }

    @Test
    @DisplayName("Unique test")
    void testUnique() throws CSVParser.ParseError, SQLException {
        CSVParser.parseCSV(impression_log,0);
        CSVParser.parseCSV(click_log,0);
        CSVParser.parseCSV(server_log,0);

        assertEquals(Database.query("SELECT COUNT(DISTINCT user_id) FROM impression_log").getString(1), "20");
        assertEquals(Database.query("SELECT COUNT(DISTINCT user_id) FROM server_log").getString(1), "10");
        assertEquals(Database.query("SELECT COUNT(DISTINCT user_id) FROM click_log").getString(1), "10");
    }

    @Test
    @DisplayName("Test age parsing")
    void ageParseTest() throws CSVParser.ParseError {
        assertEquals(CSVParser.parseAge("<25"),Common.AgeRange.U25);
        assertEquals(CSVParser.parseAge("25-34"),Common.AgeRange.F25T34);
        assertEquals(CSVParser.parseAge("35-44"),Common.AgeRange.F35T44);
        assertEquals(CSVParser.parseAge("45-54"),Common.AgeRange.F45T54);
        assertEquals(CSVParser.parseAge(">54"),Common.AgeRange.O54);
    }

    @Test
    @DisplayName("Test income parsing")
    void incomeParseTest() throws CSVParser.ParseError {
        assertEquals(CSVParser.parseIncome("low"),Common.Income.LOW);
        assertEquals(CSVParser.parseIncome("medium"),Common.Income.MEDIUM);
        assertEquals(CSVParser.parseIncome("high"),Common.Income.HIGH);
    }

    @Test
    @DisplayName("Test context parsing")
    void contextParseTest() throws CSVParser.ParseError {
        assertEquals(CSVParser.parseContext("news"),Common.Context.NEWS);
        assertEquals(CSVParser.parseContext("shopping"),Common.Context.SHOPPING);
        assertEquals(CSVParser.parseContext("social media"),Common.Context.SOCIAL_MEDIA);
        assertEquals(CSVParser.parseContext("blog"),Common.Context.BLOG);
        assertEquals(CSVParser.parseContext("hobbies"),Common.Context.HOBBIES);
        assertEquals(CSVParser.parseContext("travel"),Common.Context.TRAVEL);
    }

    @Test
    @DisplayName("Test gender parsing")
    void genderParseTest() throws CSVParser.ParseError {
        assertEquals(CSVParser.parseGender("male"),Common.Gender.MALE);
        assertEquals(CSVParser.parseGender("female"),Common.Gender.FEMALE);
    }

    @Test
    @DisplayName("Test time parsing")
    void timeParseTest() {
        assertNull(CSVParser.parseLocalDateTime("n/a"));
        assertEquals(CSVParser.parseLocalDateTime("a"),"a");
        assertEquals(CSVParser.parseLocalDateTime("2015-01-01 12:00:0"),"2015-01-01 12:00:0");
    }
}