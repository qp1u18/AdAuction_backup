package backend;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.File;
import java.sql.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;

@ParametersAreNonnullByDefault
public class Database {
    private static String DATABASE_FILE = "database.db";
    private static String DATABASE_URL = "jdbc:sqlite:" + DATABASE_FILE;

    private static Connection connection;

    /**
     * Creates a connection if one does not exist
     */
    public static Connection getConnection() throws SQLException {
        if (connection == null) {
            connection = DriverManager.getConnection(DATABASE_URL);
            connection.setAutoCommit(false);
        }
        return connection;
    }

    static void createTestConnection() throws SQLException {
        connection = null;
        DATABASE_FILE = "test_database.db";
        DATABASE_URL = "jdbc:sqlite:test_database.db";
        File f = new File(DATABASE_FILE);
        f.delete();
        Database.createSchemaIfNotExists();
    }

    public static void createSchemaIfNotExists() throws SQLException {
        if (!new File(DATABASE_FILE).exists()) {
            createSchema();
        }
    }

    /**
     * This creates the database schema if it didn't exist already. It should be run
     * before any other database related code is run
     *
     * @throws SQLException
     */
    private static void createSchema() throws SQLException {
        Connection connection = getConnection();
        Statement statement = connection.createStatement();

        String sqlClients = "CREATE TABLE IF NOT EXISTS clients (\n" +
                "id INTEGER NOT NULL PRIMARY KEY,\n" +
                "name TEXT NOT NULL,\n" +
                "password_hash TEXT NOT NULL\n" +
                ");";
        statement.executeUpdate(sqlClients);

        String sqlSettingsStore = "CREATE TABLE IF NOT EXISTS settings (\n" +
                "key TEXT NOT NULL,\n" +
                "client_id INTEGER NOT NULL,\n" +
                "value TEXT,\n" +
                "PRIMARY KEY (key, client_id),\n" +
                "FOREIGN KEY (client_id) REFERENCES clients (id)\n" +
                ");";
        statement.executeUpdate(sqlSettingsStore);

        String sqlCampaign = "CREATE TABLE IF NOT EXISTS campaigns (\n" +
                "id INTEGER NOT NULL PRIMARY KEY,\n" +
                "name TEXT NOT NULL,\n" +
                "client_id INTEGER NOT NULL,\n" +
                "FOREIGN KEY (client_id) REFERENCES clients (id)\n" +
                ");";
        statement.executeUpdate(sqlCampaign);

        String sqlUsers = "CREATE TABLE IF NOT EXISTS users (\n" +
                "id INTEGER NOT NULL PRIMARY KEY,\n" +
                "gender TEXT NOT NULL,\n" +
                "age TEXT,\n" +
                "income TEXT,\n" +
                "campaign_id INTEGER NOT NULL,\n" +
                "FOREIGN KEY (campaign_id) REFERENCES campaigns (id)" +
                ");";
        statement.executeUpdate(sqlUsers);

        String sqlClickLog = "CREATE TABLE IF NOT EXISTS click_log (\n" +
                "id INTEGER NOT NULL PRIMARY KEY,\n" +
                "date TIMESTAMP NOT NULL,\n" +
                "click_cost FLOAT NOT NULL,\n" +
                "user_id INTEGER NOT NULL,\n" +
                "campaign_id INTEGER NOT NULL,\n" +
                "FOREIGN KEY (user_id) REFERENCES users (id),\n" +
                "FOREIGN KEY (campaign_id) REFERENCES campaigns (id)" +
                ");";
        statement.executeUpdate(sqlClickLog);

        String sqlImpressionLog = "CREATE TABLE IF NOT EXISTS impression_log (\n" +
                "id INTEGER NOT NULL PRIMARY KEY,\n" +
                "date TIMESTAMP NOT NULL,\n" +
                "context TEXT NOT NULL,\n" +
                "user_id INTEGER NOT NULL,\n" +
                "impression_cost FLOAT NOT NULL,\n" +
                "campaign_id INTEGER NOT NULL,\n" +
                "FOREIGN KEY (user_id) REFERENCES users (id),\n" +
                "FOREIGN KEY (campaign_id) REFERENCES campaigns (id)" +
                ");";
        statement.executeUpdate(sqlImpressionLog);

        String sqlServerLog = "CREATE TABLE IF NOT EXISTS server_log (\n" +
                "id INTEGER NOT NULL PRIMARY KEY,\n" +
                "entry_date TIMESTAMP NOT NULL,\n" +
                "exit_date TIMESTAMP,\n" +
                "total_time INTEGER,\n" +
                "pages_viewed INTEGER NOT NULL,\n" +
                "conversion BOOLEAN NOT NULL,\n" +
                "user_id INTEGER NOT NULL,\n" +
                "campaign_id INTEGER NOT NULL,\n" +
                "FOREIGN KEY (user_id) REFERENCES users (id),\n" +
                "FOREIGN KEY (campaign_id) REFERENCES campaigns (id)" +
                ");";
        statement.executeUpdate(sqlServerLog);
        statement.close();
        try {
            Authentication.register("client1", "pass123");
            insertCampaign("Campaign 1", 1);
        } catch (Authentication.AuthenticationException e) {
            e.printStackTrace();
        }
        connection.commit();
    }

    public static void insertClient(String name, String passwordHash) throws SQLException {
        Connection connection = getConnection();
        String sql = "INSERT INTO clients(name, password_hash) VALUES(?,?)";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1, name);
        statement.setString(2, passwordHash);
        statement.executeUpdate();
        statement.close();
    }

    public static void insertCampaign(String name, int clientId) throws SQLException {
        Connection connection = getConnection();
        String sql = "INSERT INTO campaigns(name, client_id) VALUES(?,?)";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1, name);
        statement.setInt(2, clientId);
        statement.executeUpdate();
        statement.close();
    }

    static void insertImpressionLog(String date, Common.Context context, Long userId, float impressionCost, int campaignId) throws SQLException {
        List<String> dates = Collections.singletonList(date);
        List<Common.Context> contexts = Collections.singletonList(context);
        List<Long> userIds = Collections.singletonList(userId);
        List<Float> impressionCosts = Collections.singletonList(impressionCost);
        batchInsertImpressionLog(dates, contexts, userIds, impressionCosts, campaignId);
    }

    static void batchInsertImpressionLog(List<String> date, List<Common.Context> context, List<Long> userId, List<Float> impressionCost, int campaignId) throws SQLException {
        // date is a TIMESTAMP in the database, but it's faster to insert it as a label
        Connection connection = getConnection();
        String sql = "INSERT INTO impression_log(date, context, user_id, impression_cost, campaign_id) VALUES(?,?,?,?,?)";
        PreparedStatement statement = connection.prepareStatement(sql);
        for (int i = 0; i < date.size(); i++) {
            statement.setString(1, date.get(i));
            statement.setString(2, context.get(i).name());
            statement.setLong(3, userId.get(i));
            statement.setFloat(4, impressionCost.get(i));
            statement.setInt(5, campaignId);
            statement.addBatch();
        }
        statement.executeBatch();
        statement.close();
    }

    static void insertUser(Long userId, Common.Gender gender, Common.AgeRange age, Common.Income income, int campaignId) throws SQLException {
        List<Long> userIds = Collections.singletonList(userId);
        List<Common.Gender> genders = Collections.singletonList(gender);
        List<Common.AgeRange> ages = Collections.singletonList(age);
        List<Common.Income> incomes = Collections.singletonList(income);
        batchInsertUser(userIds, genders, ages, incomes, campaignId);
    }

    static void batchInsertUser(List<Long> userId, List<Common.Gender> gender, List<Common.AgeRange> age, List<Common.Income> income, int campaignId) throws SQLException {
        Connection connection = getConnection();
        String sql = "INSERT OR IGNORE INTO users(id, gender, age, income, campaign_id) VALUES (?,?,?,?,?)";
        PreparedStatement statement = connection.prepareStatement(sql);
        for (int i = 0; i < userId.size(); i++) {
            statement.setLong(1, userId.get(i));
            statement.setString(2, gender.get(i).name());
            statement.setString(3, age.get(i).name());
            statement.setString(4, income.get(i).name());
            statement.setInt(5, campaignId);
            statement.addBatch();
        }
        statement.executeBatch();
        statement.close();
    }

    static void insertServerLog(String entryDate, @Nullable String exitDate, int pagesViewed, boolean conversion, Long userId, int campaignId) throws SQLException {
        Connection connection = getConnection();
        String sql = "INSERT INTO server_log(entry_date, exit_date, total_time, pages_viewed, conversion, user_id, campaign_id) VALUES (?,?,?,?,?,?,?)";
        PreparedStatement statement = connection.prepareStatement(sql);
        //statement.setTimestamp(1, Timestamp.valueOf(entryDate));
        statement.setString(1, String.valueOf(Timestamp.valueOf(entryDate)));
        if (exitDate != null) {
            statement.setString(2, String.valueOf(Timestamp.valueOf(exitDate)));
            statement.setInt(3,getDifference(entryDate, exitDate));
        } else {
            statement.setNull(2, Types.TIMESTAMP);
            statement.setNull(3, Types.TIMESTAMP);
        }
        statement.setInt(4, pagesViewed);
        statement.setBoolean(5, conversion);
        statement.setLong(6, userId);
        statement.setInt(7, campaignId);
        statement.executeUpdate();
        statement.close();
    }

    static void insertClickLog(String date, Float clickCost, Long userId, int campaignId) throws SQLException {
        Connection connection = getConnection();
        String sql = "INSERT INTO click_log(date, click_cost, user_id, campaign_id) VALUES (?,?,?,?)";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1, date);
        statement.setFloat(2, clickCost);
        statement.setLong(3, userId);
        statement.setInt(4, campaignId);
        statement.executeUpdate();
        statement.close();
    }

    static void insertSetting(String key, String value, int client_id) throws SQLException {
        Connection connection = getConnection();
        String sql = "INSERT OR REPLACE INTO settings(key, client_id, value) VALUES (?,?,?)";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1, key);
        statement.setInt(2, client_id);
        statement.setString(3, value);
        statement.executeUpdate();
        statement.close();
    }

    static ResultSet query(String sql) throws SQLException {
        Connection connection = getConnection();
        Statement statement = connection.createStatement();
        return statement.executeQuery(sql);
    }

    public static ResultSet query(PreparedStatement statement) throws SQLException {
        return statement.executeQuery();
    }

    public static void execute(PreparedStatement statement) throws SQLException {
    	statement.executeUpdate();
	}

    public static void commitData() throws SQLException {
        connection.commit();
    }

    private static Integer getDifference(String entry, String exit) {
        java.util.Date entryDate;
        java.util.Date exitDate;
        Integer timeDifference = 0;
        try {
            DateFormat format = new SimpleDateFormat("yyy-MM-dd hh:mm:ss");
            entryDate = format.parse(entry);
            exitDate = format.parse(exit);
            timeDifference = Math.toIntExact(exitDate.getTime() - entryDate.getTime());

            //Error when parsing from 11:59:58 -> 12:00:00 and from 12:59:59 -> 13:00:00. See's it as a whole 12 hours passing.
            if(entry.split(" ")[1].split(":")[0].equals("11") && exit.split(" ")[1].split(":")[0].equals("12")) {
                timeDifference += 43200000;
            }
            if(entry.split(" ")[1].split(":")[0].equals("12") && exit.split(" ")[1].split(":")[0].equals("13")) {
                timeDifference -= 43200000;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return timeDifference;
    }
}
