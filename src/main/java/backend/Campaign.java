package backend;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class Campaign {

    private int id;
    private String name;
    public Campaign(int id) {
        this.id = id;
		PreparedStatement statement = null;
		try {
			statement = Database.getConnection().prepareStatement("SELECT name from campaigns WHERE id = ?");
			statement.setInt(1, this.id);
			ResultSet rs = Database.query(statement);
			rs.next();
			this.name = rs.getString(1);
		} catch (SQLException e) {
			e.printStackTrace();
			this.name = null;
		}

    }

    public String plusInterval(Common.Interval interval, String columnName) {
        switch (interval) {
            case MINUTE:
                return "strftime('%Y-%m-%d %H:%M', " + columnName + ")";
            case HOUR:
                return "strftime('%Y-%m-%d %H', " + columnName + ")";
            case DAY:
                return "strftime('%Y-%m-%d', " + columnName + ")";
            case WEEK:
                return "strftime('%Y %W', " + columnName + ")";
        }
        return null;
    }

    public String inclusive (Common.Interval interval){
        switch (interval) {
            case MINUTE:
                return "+1 MINUTE";
            case HOUR:
                return "+1 HOUR";
            case DAY:
                return "+1 DAY";
            case WEEK:
                return "+7 DAY";
        }
        return null;
    }

    public StringBuilder calFilter(Map<String, EnumSet<? extends Common.Property>> filters) {
        StringBuilder condition = new StringBuilder("");
        for (Map.Entry<String, EnumSet<? extends Common.Property>> entry : filters.entrySet()) {
            if(!entry.getValue().isEmpty()){
                condition.append(" AND ");
                switch (entry.getKey()){
                    case "gender":
                        condition.append(this.getOptions("gender", entry.getValue()));
                        break;
                    case "age":
                        condition.append(this.getOptions("age", entry.getValue()));
                        break;
                    case "context":
                        condition.append(this.getOptions("context", entry.getValue()));
                        break;
                    case "income":
                        condition.append(this.getOptions("income", entry.getValue()));
                        break;
                }
            }
        }
        return condition;
    }

    public String getOptions(String type,EnumSet<?> selected){
        Object[] array = selected.toArray();
        StringBuilder statement = new StringBuilder("("+type+" = '"+array[0].toString()+"'");
        for (int i=1;i<selected.size();i++){
            statement.append(" OR ").append(type).append(" = '").append(array[i].toString()).append("'");
        }
        statement.append(")");
        return  statement.toString();
    }

    public String timestamp(String formattedFrom, String formattedTo, Common.Interval interval){
        String time = "(SELECT "+ plusInterval(interval, "date") + " AS date FROM (WITH RECURSIVE "
                + "time(x) AS (VALUES('"+ formattedFrom +"') UNION ALL SELECT datetime(x, '"+ inclusive(interval) +"') FROM time WHERE (SELECT datetime(x, '"+ inclusive(interval) +"')) < '"+ formattedTo +"') "
                + "SELECT x AS date FROM time)) AS t ";
        return time;
    }

    //number of unique users that click on ad (click_log)
    public Map<String, Integer> calUniques(LocalDateTime from, LocalDateTime to, Common.Interval interval, Map<String, EnumSet<? extends Common.Property>> sqlFilter) {
        Map<String, Integer> uniques = new LinkedHashMap<>();
        try {
            DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formattedFrom = format.format(from);
            String formattedTo = format.format(to);
            StringBuilder filter = this.calFilter(sqlFilter);

            String time = timestamp(formattedFrom,formattedTo,interval);

            StringBuilder fromTable = new StringBuilder("FROM click_log ");
            String user = "";
            if(!sqlFilter.get("context").isEmpty()) {
                fromTable.append(",impression_log ");
                filter.append(" AND date(click_log.date) = date(impression_log.date)").append(" AND click_log.user_id = impression_log.user_id  AND click_log.campaign_id = impression_log.campaign_id ");
            }
            if(!sqlFilter.get("age").isEmpty()|| !sqlFilter.get("income").isEmpty()|| !sqlFilter.get("gender").isEmpty()){
                user = "LEFT JOIN users ON click_log.user_id = users.id AND click_log.campaign_id = users.campaign_id ";
            }
            String sqlUniques = "(SELECT " + plusInterval(interval, "click_log.date") + " AS date, COUNT(DISTINCT click_log.user_id) AS uniques "
                    + fromTable
                    + user
                    + "WHERE (click_log.date BETWEEN '" + formattedFrom + "' AND '" + formattedTo + "')" + filter +" AND click_log.campaign_id = " + id
                    + " GROUP BY " + plusInterval(interval, "click_log.date") + ") AS c ";


            String sql = "SELECT t.date, IFNULL(uniques,0) "
                    + "FROM "+time
                    + "LEFT JOIN "
                    + sqlUniques
                    + "ON t.date = c.date;" ;
            ResultSet rs = Database.query(sql);
            while (rs.next()) {
                uniques.put(rs.getString(1), rs.getInt(2));
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return uniques;
    }

    public Map<String, Float> calClickCost(LocalDateTime from, LocalDateTime to, Common.Interval interval, Map<String, EnumSet<? extends Common.Property>> sqlFilter) {
        Map<String, Float> clickCost = new LinkedHashMap<>();
        try {
            DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formattedFrom = format.format(from);
            String formattedTo = format.format(to);
            StringBuilder filter = this.calFilter(sqlFilter);
            StringBuilder fromTable = new StringBuilder("FROM click_log ");
            String user = "";

            String time = timestamp(formattedFrom,formattedTo,interval);

            if(!sqlFilter.get("context").isEmpty()) {
                fromTable.append(", impression_log ");
                filter.append(" AND date(click_log.date) = date(impression_log.date)").append(" AND click_log.user_id = impression_log.user_id AND click_log.campaign_id = impression_log.campaign_id ");
            }
            if(!sqlFilter.get("age").isEmpty()|| !sqlFilter.get("income").isEmpty()|| !sqlFilter.get("gender").isEmpty()){
                user = "LEFT JOIN users ON click_log.user_id = users.id AND click_log.campaign_id = users.campaign_id ";
            }
            String sqlClickCost = "(SELECT " + plusInterval(interval, "click_log.date") + " AS date, SUM(click_cost) AS click_cost "
                    + fromTable
                    + user
                    + "WHERE (click_log.date BETWEEN '" + formattedFrom + "' AND '" + formattedTo + "')"+ filter +" AND click_log.campaign_id = " + id
                    + " GROUP BY " + plusInterval(interval, "click_log.date") + ") AS c ";

            String sql = "SELECT t.date, IFNULL(click_cost,0) "
                    + "FROM "+time
                    + "LEFT JOIN "
                    + sqlClickCost
                    + "ON t.date = c.date;" ;
            ResultSet rs = Database.query(sql);
            while (rs.next()) {
                clickCost.put(rs.getString(1), rs.getFloat(2));
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return clickCost;
    }

    //number of clicks
    public Map<String, Integer> calClicks(LocalDateTime from, LocalDateTime to, Common.Interval interval, Map<String, EnumSet<? extends Common.Property>> sqlFilter) {
        Map<String, Integer> clicks = new LinkedHashMap<>();
        try {
            DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formattedFrom = format.format(from);
            String formattedTo = format.format(to);
            StringBuilder filter = this.calFilter(sqlFilter);

            String time = timestamp(formattedFrom,formattedTo,interval);

            StringBuilder fromTable = new StringBuilder("FROM click_log ");
            String user = "";
            if(!sqlFilter.get("context").isEmpty()) {
                filter.append(" AND date(click_log.date) = date(impression_log.date)").append(" AND click_log.user_id = impression_log.user_id  AND click_log.campaign_id = impression_log.campaign_id ");
                fromTable.append(",impression_log ");
            }
            if(!sqlFilter.get("age").isEmpty()|| !sqlFilter.get("income").isEmpty()|| !sqlFilter.get("gender").isEmpty()){
                user = "LEFT JOIN users ON click_log.user_id = users.id AND click_log.campaign_id = users.campaign_id ";
            }
            String sqlClicks = "(SELECT " + plusInterval(interval, "click_log.date") + " AS date, COUNT(*) AS clicks "
                    + fromTable
                    + user
                    + "WHERE (click_log.date BETWEEN '" + formattedFrom + "' AND '" + formattedTo + "')" + filter +" AND click_log.campaign_id = " + id
                    + " GROUP BY " + plusInterval(interval, "click_log.date") + ") AS c ";

            String sql = "SELECT t.date, IFNULL(clicks,0) "
                    + "FROM "+time
                    + "LEFT JOIN "
                    + sqlClicks
                    + "ON t.date = c.date;" ;
            ResultSet rs = Database.query(sql);
            while (rs.next()) {
                clicks.put(rs.getString(1), rs.getInt(2));
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return clicks;
    }

    //number of bounces(only one single page has been viewed)
    public Map<String, Integer> calBounces(LocalDateTime from, LocalDateTime to, Common.Interval interval, Map<String, EnumSet<? extends Common.Property>> sqlFilter) {
        Map<String, Integer> bounces = new LinkedHashMap<>();
        try {
            DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formattedFrom = format.format(from);
            String formattedTo = format.format(to);
            StringBuilder filter = this.calFilter(sqlFilter);
            String time = timestamp(formattedFrom,formattedTo,interval);
            StringBuilder fromTable = new StringBuilder("FROM server_log ");
            String user = "";
            if(!sqlFilter.get("context").isEmpty()) {
                fromTable.append(", impression_log ");
                filter.append(" AND date(entry_date) = date(impression_log.date)").append(" AND server_log.user_id = impression_log.user_id  AND server_log.campaign_id = impression_log.campaign_id ");
            }
            if(!sqlFilter.get("age").isEmpty()|| !sqlFilter.get("income").isEmpty()|| !sqlFilter.get("gender").isEmpty()){
                user = "LEFT JOIN users ON server_log.user_id = users.id AND server_log.campaign_id = users.campaign_id ";
            }
            String sqlBounces;
            if(Settings.getBouncerateType() == Settings.BounceRateType.PAGES) {
                sqlBounces = "(SELECT " + plusInterval(interval, "entry_date") + " AS date, COUNT(*) AS bounces "
                        + fromTable
                        + user
                        + " WHERE pages_viewed <= " + Settings.getBouncerateValue() + " AND (entry_date BETWEEN '" + formattedFrom + "' AND '" + formattedTo + "')" + filter +" AND server_log.campaign_id = " + id
                        + " GROUP BY " + plusInterval(interval, "entry_date") + ") AS c ";
            }
            else {
                sqlBounces = "(SELECT " + plusInterval(interval, "entry_date") + " AS date, COUNT(*) AS bounces "
                        + fromTable
                        + user
                        + " WHERE total_time <= " + (Settings.getBouncerateValue() * 60000) + " AND (entry_date BETWEEN '" + formattedFrom + "' AND '" + formattedTo + "')" + filter +" AND server_log.campaign_id = " + id
                        + " GROUP BY " + plusInterval(interval, "entry_date") + ") AS c ";
            }
            String sql = "SELECT t.date, IFNULL(bounces,0) "
                    + "FROM "+time
                    + "LEFT JOIN "
                    + sqlBounces
                    + "ON t.date = c.date;" ;
            ResultSet rs = Database.query(sql);
            while (rs.next()) {
                bounces.put(rs.getString(1), rs.getInt(2));
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return bounces;
    }

    //number of conversions ("Yes")
    public Map<String, Integer> calConversions(LocalDateTime from, LocalDateTime to, Common.Interval interval, Map<String, EnumSet<? extends Common.Property>> sqlFilter) {

        Map<String, Integer> conversions = new LinkedHashMap<>();
        try {
            DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formattedFrom = format.format(from);
            String formattedTo = format.format(to);
            StringBuilder filter = this.calFilter(sqlFilter);
            StringBuilder fromTable = new StringBuilder("FROM server_log ");
            String user = "";

            String time = timestamp(formattedFrom,formattedTo,interval);

            if(!sqlFilter.get("context").isEmpty()) {
                fromTable.append(", impression_log ");
                filter.append(" AND date(entry_date) = date(impression_log.date)").append(" AND server_log.user_id = impression_log.user_id  AND server_log.campaign_id = impression_log.campaign_id ");
            }
            if(!sqlFilter.get("age").isEmpty()|| !sqlFilter.get("income").isEmpty()|| !sqlFilter.get("gender").isEmpty()){
                user = "LEFT JOIN users ON server_log.user_id = users.id AND server_log.campaign_id = users.campaign_id ";
            }
            String sqlConversions = "(SELECT " + plusInterval(interval, "entry_date") + " AS date, COUNT(*) AS conversions "
                    + fromTable
                    + user
                    + "WHERE conversion = 1 AND (entry_date BETWEEN '" + formattedFrom + "' AND '" + formattedTo + "')" + filter +" AND server_log.campaign_id = " + id
                    + " GROUP BY " + plusInterval(interval, "entry_date") + ") AS c ";

            String sql = "SELECT t.date, IFNULL(conversions,0) "
                    + "FROM "+time
                    + "LEFT JOIN "
                    + sqlConversions
                    + "ON t.date = c.date;" ;
            ResultSet rs = Database.query(sql);
            while (rs.next()) {
                conversions.put(rs.getString(1), rs.getInt(2));
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return conversions;
    }

    public Map<String, Integer> calImpressions(LocalDateTime from, LocalDateTime to, Common.Interval interval, Map<String, EnumSet<? extends Common.Property>> sqlFilter)   //from..to.. (interval e.g. 1min,1h,1day)
    {
        Map<String, Integer> impressions = new LinkedHashMap<>();
        try {
            DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formattedFrom = format.format(from);
            String formattedTo = format.format(to);
            StringBuilder filter = this.calFilter(sqlFilter);
            String user = "";
            String time = timestamp(formattedFrom,formattedTo,interval);

            if(!sqlFilter.get("age").isEmpty()|| !sqlFilter.get("income").isEmpty()|| !sqlFilter.get("gender").isEmpty()){
                user = "LEFT JOIN users ON impression_log.user_id = users.id AND impression_log.campaign_id = users.campaign_id ";
            }
            String sqlImpression = "(SELECT " + plusInterval(interval, "date") + " AS date ,COUNT(*) AS impressions "
                    + "FROM impression_log "
                    + user
                    + "WHERE (date BETWEEN '" + formattedFrom + "' AND '" + formattedTo + "')" + filter +" AND impression_log.campaign_id = " + id
                    + " GROUP BY " + plusInterval(interval, "date") + " ) AS c ";

            String sql = "SELECT t.date, IFNULL(impressions,0) "
                    + "FROM "+time
                    + "LEFT JOIN "
                    + sqlImpression
                    + "ON t.date = c.date;" ;
            ResultSet rs = Database.query(sql);
            while (rs.next()) {
                impressions.put(rs.getString(1), rs.getInt(2));
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return impressions;
    }

    public Map<String, Float> calImpressionCost(LocalDateTime from, LocalDateTime to, Common.Interval interval, Map<String, EnumSet<? extends Common.Property>> sqlFilter) {
        Map<String, Float> impressionCost = new LinkedHashMap<>();
        try {
            DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formattedFrom = format.format(from);
            String formattedTo = format.format(to);
            StringBuilder filter = this.calFilter(sqlFilter);
            String time = timestamp(formattedFrom,formattedTo,interval);
            String user = "";
            if(!sqlFilter.get("age").isEmpty()|| !sqlFilter.get("income").isEmpty()|| !sqlFilter.get("gender").isEmpty()){
                user = "LEFT JOIN users ON impression_log.user_id = users.id AND impression_log.campaign_id = users.campaign_id ";
            }
            String sqlImpressionCost = "(SELECT " + plusInterval(interval, "date") + " AS date, SUM(impression_cost) AS impression_cost "
                    + "FROM impression_log "
                    + user
                    + "WHERE (date BETWEEN '" + formattedFrom + "' AND '" + formattedTo + "')" + filter +" AND impression_log.campaign_id = " + id
                    + " GROUP BY " + plusInterval(interval, "date") + ") AS c ";

            String sql = "SELECT t.date, IFNULL(impression_cost,0) "
                    + "FROM "+time
                    + "LEFT JOIN "
                    + sqlImpressionCost
                    + "ON t.date = c.date;" ;
            ResultSet rs = Database.query(sql);
            while (rs.next()) {
                impressionCost.put(rs.getString(1), rs.getFloat(2));
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return impressionCost;
    }


    public Map<String, Float> calTotalCost(LocalDateTime from, LocalDateTime to, Common.Interval interval, Map<String, EnumSet<? extends Common.Property>> sqlFilter) {
        Map<String, Float> totalCost = new LinkedHashMap<>();
        Map<String, Float> clickCost = this.calClickCost(from, to, interval, sqlFilter);
        Map<String, Float> impressionCost = this.calImpressionCost(from, to, interval, sqlFilter);

        for (Map.Entry<String, Float> entry1 : clickCost.entrySet()) {
            String key = entry1.getKey();
            Float value1 = entry1.getValue();
            Float value2 = impressionCost.get(key);
            totalCost.put(key, value1 + value2);
        }
        return totalCost;
    }

    // the average number of clicks per impression
    public Map<String, Float> calCTR(LocalDateTime from, LocalDateTime to, Common.Interval interval, Map<String, EnumSet<? extends Common.Property>> sqlFilter) {
        Map<String, Float> CTR = new LinkedHashMap<>();
        Map<String, Integer> clicks = this.calClicks(from, to, interval, sqlFilter);
        Map<String, Integer> impression = this.calImpressions(from, to, interval, sqlFilter);
        for (Map.Entry<String, Integer> entry1 : clicks.entrySet()) {
            String key = entry1.getKey();
            Integer value1 = entry1.getValue();
            Integer value2 = impression.get(key);
            if(value2==0){
                CTR.put(key,(float)0);
            }
            else{
                CTR.put(key, value1 / (float) value2);
            }
        }
        return CTR;

    }

    //the average amount of money spent on conversion
    public Map<String, Float> calCPA(LocalDateTime from, LocalDateTime to, Common.Interval interval, Map<String, EnumSet<? extends Common.Property>> sqlFilter) {
        Map<String, Float> CPA = new LinkedHashMap<>();
        Map<String, Float> totalCost = this.calTotalCost(from, to, interval, sqlFilter);
        Map<String, Integer> conversion = this.calConversions(from, to, interval, sqlFilter);
        for (Map.Entry<String, Float> entry1 : totalCost.entrySet()) {
            String key = entry1.getKey();
            Float cost = entry1.getValue();
            Integer con = conversion.get(key);
            if(con==0){
                CPA.put(key,(float)0);
            }
            else{
                CPA.put(key, cost / (float)con);
            }
        }
        return CPA;
    }

    // the average amount of money spent on each click
    public Map<String, Float> calCPC(LocalDateTime from, LocalDateTime to, Common.Interval interval, Map<String, EnumSet<? extends Common.Property>> sqlFilter) {
        Map<String, Float> CPC = new LinkedHashMap<>();
        Map<String, Float> totalClickCost = this.calClickCost(from, to, interval, sqlFilter);
        Map<String, Integer> clicks = this.calClicks(from, to, interval, sqlFilter);
        for (Map.Entry<String, Float> entry1 : totalClickCost.entrySet()) {
            String key = entry1.getKey();
            Float cost = entry1.getValue();
            Integer cl = clicks.get(key);
            if(cl==0){
                CPC.put(key,(float)0);
            }
            else{
                CPC.put(key, cost / (float) cl);
            }
        }
        return CPC;
    }

    // the average amount of money spent on a campaign for every one thousand impressions
    public Map<String, Float> calCPM(LocalDateTime from, LocalDateTime to, Common.Interval interval, Map<String, EnumSet<? extends Common.Property>> sqlFilter) {
        Map<String, Float> CPM = new LinkedHashMap<>();
        Map<String, Float> imCost = this.calImpressionCost(from, to, interval, sqlFilter);
        Map<String, Integer> impression = this.calImpressions(from, to, interval, sqlFilter);
        for (Map.Entry<String, Float> entry1 : imCost.entrySet()) {
            String key = entry1.getKey();
            Float cost = entry1.getValue();
            Integer im = impression.get(key);
            if(im==0){
                CPM.put(key, (float) 0);
            }
            else{
                CPM.put(key, (cost / (float) im) * 1000);
            }
        }
        return CPM;
    }

    //the average number of bounces per click
    public Map<String, Float> calBounceRate(LocalDateTime from, LocalDateTime to, Common.Interval interval, Map<String, EnumSet<? extends Common.Property>> sqlFilter) {
        Map<String, Float> bounceRate = new LinkedHashMap<>();
        Map<String, Integer> clicks = this.calClicks(from, to, interval, sqlFilter);
        Map<String, Integer> bounces = this.calBounces(from, to, interval, sqlFilter);
        for (Map.Entry<String, Integer> entry1 : clicks.entrySet()) {
            String key = entry1.getKey();
            Integer cl = entry1.getValue();
            Integer b = bounces.get(key);
            if(cl==0){
                bounceRate.put(key,(float)0);
            }
            else{
                bounceRate.put(key, b / (float) cl);
            }
        }
        return bounceRate;
    }

    public static List<Campaign> getCampaigns(int clientId) throws SQLException {
    	List<Campaign> out = new ArrayList<>();
		PreparedStatement statement;
    	if (clientId == 0) {
			statement = Database.getConnection().prepareStatement("SELECT id from campaigns");
		}
    	else {
			statement = Database.getConnection().prepareStatement("SELECT id from campaigns WHERE client_id = ?");
			statement.setInt(1, clientId);
		}
		ResultSet resultSet = Database.query(statement);
		while (resultSet.next()) {
			out.add(new Campaign(resultSet.getInt(1)));
		}
		return out;
	}

	public static Campaign getFromName(String name) throws SQLException {
		PreparedStatement statement = Database.getConnection().prepareStatement("SELECT id from campaigns WHERE name = ?");
		statement.setString(1, name);
		ResultSet resultSet = Database.query(statement);
		resultSet.next();
		return new Campaign(resultSet.getInt(1));
	}

	public String getName() {
		return name;
	}

	public int getId() {
		return id;
	}

	public static ObservableList<String> campaignNameList(int clientId) {
		try {
			List<Campaign> campaigns = Campaign.getCampaigns(clientId);
			List<String> campaignNames = campaigns.stream().map(Campaign::getName).collect(Collectors.toList());
			return FXCollections.observableArrayList(campaignNames);
		} catch (SQLException e) {
			e.printStackTrace();
			return FXCollections.observableArrayList();
		}
	}

	public void delete() throws SQLException {
    	PreparedStatement statement = Database.getConnection().prepareStatement("DELETE FROM campaigns WHERE id = ?");
    	statement.setInt(1, this.id);
    	Database.execute(statement);
    	Database.commitData();
	}
}

