package backend;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads CSV files
 */
public class CSVParser {

    /**
     * @param filePath the path of the CSV file to parse
     * @param campaignId the ID of the campaign these files belong to
     * @throws ParseError thrown if the file cannot be parsed/stored for any reason
     */
    public static void parseCSV(String filePath, int campaignId) throws ParseError {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));
            switch (reader.readLine().split(",").length) {
                case (3):
                    parseClickLog(reader, campaignId);
                    break;
                case (5):
                    parseServerLog(reader, campaignId);
                    break;
                case (7):
                    parseImpressionLog(reader, campaignId);
                    break;
                default:
                    throw new ParseError("Invalid number of columns");
            }
            Database.commitData();
        }
        catch (SQLException | IOException ex) {
            throw new ParseError(ex);
        }
    }

    private static void parseClickLog(BufferedReader reader, int campaignId) throws IOException, SQLException, ParseError {
        String line;
        while ((line = reader.readLine()) != null) {
            String[] lineData = line.split(",");
            String date = parseLocalDateTime(lineData[0]);
            if (date == null) throw new ParseError("Date cannot be n/a");
            if (Float.parseFloat(lineData[2]) < 0 || Long.parseLong(lineData[1]) < 0) throw new ParseError("Must have positive values");
            Database.insertClickLog(date, Float.valueOf(lineData[2]), Long.valueOf(lineData[1]), campaignId);
        }
    }

    private static void parseServerLog(BufferedReader reader, int campaignId) throws IOException, SQLException, ParseError {
        String line;
        while ((line = reader.readLine()) != null) {
            String[] lineData = line.split(",");
            String date = parseLocalDateTime(lineData[0]);
            if (date == null) throw new ParseError("Start date cannot be n/a");
            if (Integer.parseInt(lineData[3]) < 0) throw new ParseError("Must have positive values");
            Database.insertServerLog(date, parseLocalDateTime(lineData[2]), Integer.parseInt(lineData[3]), (lineData[4].equalsIgnoreCase("Yes")), Long.valueOf(lineData[1]), campaignId);
        }
    }

    private static void parseImpressionLog(BufferedReader reader, int campaignId) throws IOException, SQLException, ParseError {
        String line;
        List<Long> userIds = new ArrayList<>();
        List<Common.Gender> genders = new ArrayList<>();
        List<Common.AgeRange> ages = new ArrayList<>();
        List<Common.Income> incomes = new ArrayList<>();
        List<String> dates = new ArrayList<>();
        List<Common.Context> contexts = new ArrayList<>();
        List<Float> impressionCosts = new ArrayList<>();
        while ((line = reader.readLine()) != null) {
            String[] lineData = line.split(",");
            if(Float.parseFloat(lineData[6]) < 0.0 || Long.parseLong(lineData[1]) < 0) throw new ParseError("Must have positive values");
            userIds.add(Long.valueOf(lineData[1]));
            genders.add(parseGender(lineData[2]));
            ages.add(parseAge(lineData[3]));
            incomes.add(parseIncome(lineData[4]));
            dates.add(parseLocalDateTime(lineData[0]));
            contexts.add(parseContext(lineData[5]));
            impressionCosts.add(Float.valueOf(lineData[6]));
        }
        Database.batchInsertUser(userIds, genders, ages, incomes, campaignId);
        Database.batchInsertImpressionLog(dates, contexts, userIds, impressionCosts, campaignId);
    }

    public static Common.AgeRange parseAge(String age) throws ParseError {
        switch (age) {
            case ("<25"):
                return Common.AgeRange.U25;
            case ("25-34"):
                return Common.AgeRange.F25T34;
            case ("35-44"):
                return Common.AgeRange.F35T44;
            case ("45-54"):
                return Common.AgeRange.F45T54;
            case (">54"):
                return Common.AgeRange.O54;
            default:
                throw new ParseError("Invalid age range: " + age);
        }
    }

    public static Common.Income parseIncome(String income) throws ParseError {
        switch (income.toUpperCase()) {
            case ("LOW"):
                return Common.Income.LOW;
            case ("MEDIUM"):
                return Common.Income.MEDIUM;
            case ("HIGH"):
                return Common.Income.HIGH;
            default:
                throw new ParseError("Invalid income: " + income);
        }
    }

    public static Common.Context parseContext(String context) throws ParseError {
        switch (context.toUpperCase()) {
            case ("NEWS"):
                return Common.Context.NEWS;
            case ("SHOPPING"):
                return Common.Context.SHOPPING;
            case ("SOCIAL MEDIA"):
                return Common.Context.SOCIAL_MEDIA;
            case ("BLOG"):
                return Common.Context.BLOG;
            case ("HOBBIES"):
                return Common.Context.HOBBIES;
            case ("TRAVEL"):
                return Common.Context.TRAVEL;
            default:
                throw new ParseError("Invalid context: " + context);
        }
    }

    public static Common.Gender parseGender(String gender) throws ParseError {
        switch (gender.toUpperCase()) {
            case ("MALE"):
                return Common.Gender.MALE;
            case ("FEMALE"):
                return Common.Gender.FEMALE;
            default:
                throw new ParseError("Invalid gender: " + gender);
        }
    }

    public static Common.Interval parseInterval(String interval) throws ParseError {
        switch(interval.toUpperCase()) {
            case ("MINUTE"):
                return Common.Interval.MINUTE;
            case ("HOUR"):
                return Common.Interval.HOUR;
            case ("DAY"):
                return Common.Interval.DAY;
            case ("WEEK"):
                return Common.Interval.WEEK;
            default:
                throw new ParseError("Invalid interval" + interval);
        }
    }

    static String parseLocalDateTime(String time) {
        if (time.equals("n/a")) {
            return null;
        }
        return time;
    }

    public static class ParseError extends Exception {
        ParseError(String s) {
            super(s);
        }

        ParseError(Throwable throwable) {
            super(throwable);
        }
    }
}