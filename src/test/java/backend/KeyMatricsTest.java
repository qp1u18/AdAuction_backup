package backend;

import org.junit.jupiter.api.*;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;

public class KeyMatricsTest {

    private Campaign c = new Campaign(1);
    private Map<String, EnumSet<? extends Common.Property>> filters = new LinkedHashMap<String, EnumSet<? extends Common.Property>>();
    EnumSet<Common.Gender> filter1 = EnumSet.noneOf(Common.Gender.class);
    EnumSet<Common.Income> filter2 = EnumSet.noneOf(Common.Income.class);
    EnumSet<Common.AgeRange> filter3 = EnumSet.noneOf(Common.AgeRange.class);
    EnumSet<Common.Context> filter4 = EnumSet.noneOf(Common.Context.class);
    public KeyMatricsTest(){
        filters.put("gender", filter1);
        filters.put("income", filter2);
        filters.put("age", filter3);
        filters.put("context",filter4);
    }

    @BeforeAll
    public static void setUp() throws SQLException, CSVParser.ParseError, Authentication.AuthenticationException {
        Database.createTestConnection();
        String impression_log = "test_data/impression_log_test.csv";
        String server_log = "test_data/server_log_test.csv";
        String click_log = "test_data/click_log_test.csv";
        CSVParser.parseCSV(impression_log, 1);
        CSVParser.parseCSV(click_log,1);
        CSVParser.parseCSV(server_log,1);

        Database.insertImpressionLog("2015-01-02 12:00:00",backend.Common.Context.BLOG, 399593948382193664L, (float) 0.005,1);
        Database.insertImpressionLog("2015-01-02 14:00:00",backend.Common.Context.BLOG, 5694894591373382656L, (float) 0.001,0);
        Database.insertImpressionLog("2015-01-09 15:00:00",backend.Common.Context.BLOG, 8213940264363045888L, (float) 0.002,1);

        Database.insertClickLog("2015-01-01 12:04:00", (float) 10,5242265658534560768L,1); //2015-01-01 12:00:26,5242265658534560768,Male,45-54,Medium,Social Media,0.002314
        Database.insertClickLog("2015-01-02 12:02:00", (float) 10,5217170615204436992L,1);
        Database.insertClickLog("2015-01-02 14:00:00", (float) 20,9205559084602150912L,0);
        Database.insertClickLog("2015-01-09 15:00:00", (float) 30,5694894591373382656L,1);

        Database.insertServerLog("2015-01-02 12:00:00","2015-01-02 16:00:00",1,true,399593948382193664L,1);
        Database.insertServerLog("2015-01-02 14:00:00","2015-01-06 16:00:00",6,false,5694894591373382656L,0);
        Database.insertServerLog("2015-01-09 14:00:00","2015-01-10 16:00:00",1,true,9205559084602150912L,1);

        Authentication.register("test","test");
        Authentication.login("test","test");

    }




    @Test
    @DisplayName("Test calculation with unsuitable granularity")
    void calUniquesUnsuitableIntervalTest() {
        LocalDateTime from = LocalDateTime.of(2015, 1, 1, 12, 1,0);
        LocalDateTime to = LocalDateTime.of(2015, 1, 1, 12, 2,0);
        Map<String, Integer> expected = new LinkedHashMap<>();
        expected.put("2015-01-01 12",2);
        Assertions.assertEquals(expected,c.calUniques(from,to, Common.Interval.HOUR,filters));
        expected.clear();
        expected.put("2015-01-01",2);
        Assertions.assertEquals(expected,c.calUniques(from,to, Common.Interval.DAY,filters));
        expected.clear();
        expected.put("2015 00",2);
        Assertions.assertEquals(expected,c.calUniques(from,to, Common.Interval.WEEK,filters));
    }

    @Test
    @DisplayName("Test Uniques_Minute")
    void calUniquesMinuteTest() {
        LocalDateTime from = LocalDateTime.of(2015, 1, 1, 12, 1);
        LocalDateTime to = LocalDateTime.of(2015, 1, 1, 12, 2);
        Map<String, Integer> expected = new LinkedHashMap<>();
        expected.put("2015-01-01 12:01",2);
        Assertions.assertEquals(expected,c.calUniques(from,to, Common.Interval.MINUTE,filters));
    }

    @Test
    @DisplayName("Test Uniques_Hour")
    void calUniquesHourTest() {
        LocalDateTime from = LocalDateTime.of(2015, 1, 1, 12, 0);
        LocalDateTime to = LocalDateTime.of(2015, 1, 1, 13, 0);
        Map<String, Integer> expected = new LinkedHashMap<>();
        expected.put("2015-01-01 12",11);
        Assertions.assertEquals(expected,c.calUniques(from,to, Common.Interval.HOUR,filters));
    }

    @Test
    @DisplayName("Test Uniques_Day")
    void calUniquesTest() {
        LocalDateTime from = LocalDateTime.of(2015, 1, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2015, 1, 2, 0, 0);
        Map<String, Integer> expected = new LinkedHashMap<>();
        expected.put("2015-01-01",11);
        Assertions.assertEquals(expected,c.calUniques(from,to, Common.Interval.DAY,filters));
    }

    @Test
    @DisplayName("Test Uniques_Week")
    void calUniquesWeekTest() {
        LocalDateTime from = LocalDateTime.of(2015, 1, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2015, 1, 7, 0, 0);
        Map<String, Integer> expected = new LinkedHashMap<>();
        expected.put("2015 00",11);
        Assertions.assertEquals(expected,c.calUniques(from,to, Common.Interval.WEEK,filters));
    }

    @Test
    @DisplayName("Test Uniques_Hour_Gender")
    void calUniquesHourGenderTest() {
        LocalDateTime from = LocalDateTime.of(2015, 1, 1, 12, 0);
        LocalDateTime to = LocalDateTime.of(2015, 1, 1, 13, 0);
        filter1.add(Common.Gender.MALE);
        Map<String, Integer> expected = new LinkedHashMap<>();
        expected.put("2015-01-01 12",1);
        Assertions.assertEquals(expected,c.calUniques(from,to, Common.Interval.HOUR,filters));

        filter1.clear();
        filter1.add(Common.Gender.FEMALE);
        expected.clear();
        expected.put("2015-01-01 12",0);
        Assertions.assertEquals(expected,c.calUniques(from,to, Common.Interval.HOUR,filters));
        filter1.clear();
    }

    @Test
    @DisplayName("Test Uniques_Hour_Income")
    void calUniquesHourIncomeTest() {
        LocalDateTime from = LocalDateTime.of(2015, 1, 1, 12, 0);
        LocalDateTime to = LocalDateTime.of(2015, 1, 1, 13, 0);
        filter2.add(Common.Income.LOW);
        Map<String, Integer> expected = new LinkedHashMap<>();
        expected.put("2015-01-01 12",0);
        Assertions.assertEquals(expected,c.calUniques(from,to, Common.Interval.HOUR,filters));

        filter2.clear();
        filter2.add(Common.Income.MEDIUM);
        expected.clear();
        expected.put("2015-01-01 12",1);
        Assertions.assertEquals(expected,c.calUniques(from,to, Common.Interval.HOUR,filters));

        filter2.clear();
        filter2.add(Common.Income.HIGH);
        expected.clear();
        expected.put("2015-01-01 12",0);
        Assertions.assertEquals(expected,c.calUniques(from,to, Common.Interval.HOUR,filters));
        filter2.clear();
    }

    @Test
    @DisplayName("Test Uniques_Hour_Age")
    void calUniquesHourAgeTest() {
        LocalDateTime from = LocalDateTime.of(2015, 1, 1, 12, 0);
        LocalDateTime to = LocalDateTime.of(2015, 1, 1, 13, 0);
        Map<String, Integer> expected = new LinkedHashMap<>();

        filter3.add(Common.AgeRange.U25);
        expected.put("2015-01-01 12",0);
        Assertions.assertEquals(expected,c.calUniques(from,to, Common.Interval.HOUR,filters));

        filter3.add(Common.AgeRange.F25T34);
        expected.clear();
        expected.put("2015-01-01 12",0);
        Assertions.assertEquals(expected,c.calUniques(from,to, Common.Interval.HOUR,filters));

        filter3.clear();
        filter3.add(Common.AgeRange.F35T44);
        expected.clear();
        expected.put("2015-01-01 12",0);
        Assertions.assertEquals(expected,c.calUniques(from,to, Common.Interval.HOUR,filters));

        filter3.clear();
        filter3.add(Common.AgeRange.F45T54);
        expected.clear();
        expected.put("2015-01-01 12",1);
        Assertions.assertEquals(expected,c.calUniques(from,to, Common.Interval.HOUR,filters));

        filter3.clear();
        filter3.add(Common.AgeRange.O54);
        expected.clear();
        expected.put("2015-01-01 12",0);
        Assertions.assertEquals(expected,c.calUniques(from,to, Common.Interval.HOUR,filters));
        filter3.clear();
    }

    @Test
    @DisplayName("Test Uniques_Hour_Context")
    void calUniquesHourContextTest() {
        LocalDateTime from = LocalDateTime.of(2015, 1, 1, 12, 0);
        LocalDateTime to = LocalDateTime.of(2015, 1, 1, 13, 0);
        Map<String, Integer> expected = new LinkedHashMap<>();

        filter4.add(Common.Context.BLOG);
        expected.put("2015-01-01 12",0);
        Assertions.assertEquals(expected,c.calUniques(from,to, Common.Interval.HOUR,filters));

        filter4.add(Common.Context.HOBBIES);
        expected.clear();
        expected.put("2015-01-01 12",0);
        Assertions.assertEquals(expected,c.calUniques(from,to, Common.Interval.HOUR,filters));

        filter4.clear();
        filter4.add(Common.Context.NEWS);
        expected.clear();
        expected.put("2015-01-01 12",0);
        Assertions.assertEquals(expected,c.calUniques(from,to, Common.Interval.HOUR,filters));

        filter4.clear();
        filter4.add(Common.Context.SHOPPING);
        expected.clear();
        expected.put("2015-01-01 12",0);
        Assertions.assertEquals(expected,c.calUniques(from,to, Common.Interval.HOUR,filters));

        filter4.clear();
        filter4.add(Common.Context.SOCIAL_MEDIA);
        expected.clear();
        expected.put("2015-01-01 12",1);
        Assertions.assertEquals(expected,c.calUniques(from,to, Common.Interval.HOUR,filters));

        filter4.clear();
        filter4.add(Common.Context.TRAVEL);
        expected.clear();
        expected.put("2015-01-01 12",0);
        Assertions.assertEquals(expected,c.calUniques(from,to, Common.Interval.HOUR,filters));
        filter4.clear();
    }

    @Test
    @DisplayName("Test boundary of calculation with minute granularity")
    void calUniquesMinuteTestInclusive() {
        LocalDateTime from = LocalDateTime.of(2015, 1, 1, 12, 1,0);
        LocalDateTime to = LocalDateTime.of(2015, 1, 1, 12, 1,0);
        Map<String, Integer> expected = new LinkedHashMap<>();
        expected.put("2015-01-01 12:01",0);
        Assertions.assertEquals(expected,c.calUniques(from,to, Common.Interval.MINUTE,filters));
    }
    @Test
    @DisplayName("Test Click_Cost_Day")
    void calClickCostTest() {
        LocalDateTime from = LocalDateTime.of(2015, 1, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2015, 1, 2, 0, 0);
        Map<String, Float> expected = new LinkedHashMap<>();
        expected.put("2015-01-01", (float) 52.681084);
        Assertions.assertEquals(expected,c.calClickCost(from,to, Common.Interval.DAY,filters));
    }

    @Test
    @DisplayName("Test Clicks_Day")
    void calClicksTest() {
        LocalDateTime from = LocalDateTime.of(2015, 1, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2015, 1, 2, 0, 0);
        Map<String, Integer> expected = new LinkedHashMap<>();
        expected.put("2015-01-01",11);
        Assertions.assertEquals(expected,c.calClicks(from,to, Common.Interval.DAY,filters));
    }



    @Test
    @DisplayName("Test Bounces_1_PAGE_Viewed_Day")
    void calBouncesOnePageTest() throws Authentication.AuthenticationException {
        LocalDateTime from = LocalDateTime.of(2015, 1, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2015, 1, 2, 0, 0);
        Map<String, Integer> expected = new LinkedHashMap<>();

        Settings.setBouncerateType(Settings.BounceRateType.PAGES);
        Settings.setBouncerateValue(1);
        expected.put("2015-01-01",2);
        Assertions.assertEquals(expected,c.calBounces(from,to, Common.Interval.DAY,filters));
    }
    @Test
    @DisplayName("Test Bounces_3_PAGE_Viewed_Day")
    void calBouncesThreePageTest() throws Authentication.AuthenticationException {
        LocalDateTime from = LocalDateTime.of(2015, 1, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2015, 1, 2, 0, 0);
        Map<String, Integer> expected = new LinkedHashMap<>();

        Settings.setBouncerateType(Settings.BounceRateType.PAGES);
        Settings.setBouncerateValue(3);
        expected.put("2015-01-01",3);
        Assertions.assertEquals(expected,c.calBounces(from,to, Common.Interval.DAY,filters));
    }

    @Test
    @DisplayName("Test Bounces_1_TIME_Viewed_Day")
    void calBouncesOneTimeTest() throws Authentication.AuthenticationException {
        LocalDateTime from = LocalDateTime.of(2015, 1, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2015, 1, 2, 0, 0);
        Map<String, Integer> expected = new LinkedHashMap<>();

        Settings.setBouncerateType(Settings.BounceRateType.TIME);
        Settings.setBouncerateValue(1);
        expected.put("2015-01-01",2);
        Assertions.assertEquals(expected,c.calBounces(from,to, Common.Interval.DAY,filters));
    }
    @Test
    @DisplayName("Test Bounces_3_TIME_Viewed_Day")
    void calBouncesThreeTimeTest() throws Authentication.AuthenticationException {
        LocalDateTime from = LocalDateTime.of(2015, 1, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2015, 1, 2, 0, 0);
        Map<String, Integer> expected = new LinkedHashMap<>();

        Settings.setBouncerateType(Settings.BounceRateType.TIME);
        Settings.setBouncerateValue(3);
        expected.put("2015-01-01",6);
        Assertions.assertEquals(expected,c.calBounces(from,to, Common.Interval.DAY,filters));
    }


    @Test
    @DisplayName("Test Conversions_Day")
    void calConversionsTest() {
        LocalDateTime from = LocalDateTime.of(2015, 1, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2015, 1, 2, 0, 0);
        Map<String, Integer> expected = new LinkedHashMap<>();
        expected.put("2015-01-01",2);
        Assertions.assertEquals(expected,c.calConversions(from,to, Common.Interval.DAY,filters));
    }

    @Test
    @DisplayName("Test Impressions_Day")
    void calImpressionsTest() {
        LocalDateTime from = LocalDateTime.of(2015, 1, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2015, 1, 2, 0, 0);
        Map<String, Integer> expected = new LinkedHashMap<>();
        expected.put("2015-01-01",20);
        Assertions.assertEquals(expected,c.calImpressions(from,to, Common.Interval.DAY,filters));
    }

    @Test
    @DisplayName("Test Impression_Cost_Day")
    void calImpressionCostTest() {
        LocalDateTime from = LocalDateTime.of(2015, 1, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2015, 1, 2, 0, 0);
        Map<String, Float> expected = new LinkedHashMap<>();
        expected.put("2015-01-01", (float) 0.032339);
        Assertions.assertEquals(expected,c.calImpressionCost(from,to, Common.Interval.DAY,filters));
    }

    @Test
    @DisplayName("Test Total_Cost_Day")
    void calTotalCostTest() {
        LocalDateTime from = LocalDateTime.of(2015, 1, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2015, 1, 2, 0, 0);
        Map<String, Float> expected = new LinkedHashMap<>();
        expected.put("2015-01-01", (float) 52.71342);
        Assertions.assertEquals(expected,c.calTotalCost(from,to, Common.Interval.DAY,filters));
    }

    @Test
    @DisplayName("Test CTR_Day")
    void calCTRTest() {
        LocalDateTime from = LocalDateTime.of(2015, 1, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2015, 1, 2, 0, 0);
        Map<String, Float> expected = new LinkedHashMap<>();
        expected.put("2015-01-01", (float) 0.55);
        Assertions.assertEquals(expected,c.calCTR(from,to, Common.Interval.DAY,filters));
    }

    @Test
    @DisplayName("Test CPA_Day")
    void calCPATest() {
        LocalDateTime from = LocalDateTime.of(2015, 1, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2015, 1, 2, 0, 0);
        Map<String, Float> expected = new LinkedHashMap<>();
        expected.put("2015-01-01", (float) 26.35671);
        Assertions.assertEquals(expected,c.calCPA(from,to, Common.Interval.DAY,filters));
    }

    @Test
    @DisplayName("Test CPC_Day")
    void calCPCTest() {
        LocalDateTime from = LocalDateTime.of(2015, 1, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2015, 1, 2, 0, 0);
        Map<String, Float> expected = new LinkedHashMap<>();
        expected.put("2015-01-01", (float) 4.7891893);
        Assertions.assertEquals(expected,c.calCPC(from,to, Common.Interval.DAY,filters));
    }

    @Test
    @DisplayName("Test CPM_Day")
    void calCPMTest() {
        LocalDateTime from = LocalDateTime.of(2015, 1, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2015, 1, 2, 0, 0);
        Map<String, Float> expected = new LinkedHashMap<>();
        expected.put("2015-01-01", (float) 1.6169499);
        Assertions.assertEquals(expected,c.calCPM(from,to, Common.Interval.DAY,filters));
    }

    @Test
    @DisplayName("Test Bounce_Rate_1_Page_Viewed_Day")
    void calBouncesRateTest() {
        LocalDateTime from = LocalDateTime.of(2015, 1, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2015, 1, 2, 0, 0);
        Map<String, Float> expected = new LinkedHashMap<>();
        Settings.setBouncerateType(Settings.BounceRateType.PAGES);
        Settings.setBouncerateValue(1);
        expected.put("2015-01-01", (float) 0.18181819);
        Assertions.assertEquals(expected,c.calBounceRate(from,to, Common.Interval.DAY,filters));
    }

    @Test
    @DisplayName("Test Delete Campaign")
    void deleteCampaign() throws SQLException {
        c.delete();
        PreparedStatement statement = null;
        statement = Database.getConnection().prepareStatement("SELECT COUNT(*) from campaigns WHERE id = ?");
        statement.setInt(1, c.getId());
        ResultSet rs = Database.query(statement);
        rs.next();
        Assertions.assertEquals(0,rs.getInt(1));
    }


    @AfterAll
    public static void testEnd() {
        File f = new File("test_database.db");
        f.delete();
    }


}
