package backend;


import frontend.FrontendConfig;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Settings {
	private static final String BOUNCE_RATE_TYPE = "BOUNCE_RATE_TYPE";
	private static final String BOUNCE_RATE_VALUE = "BOUNCE_RATE_VALUE";
	private  static final String CHART_KEY = "CHART_SAVE";
	private static final String LEGAL = "LEGAL";
	private static final String FRONTEND_CONFIG_KEY = "FRONTEND_CONFIG";

	public static BounceRateType getBouncerateType() {
		String bounceRateType = getString(BOUNCE_RATE_TYPE);
		if (bounceRateType == null) {
			return BounceRateType.PAGES;
		}
		return BounceRateType.valueOf(bounceRateType);
	}

	public static void setBouncerateType(BounceRateType bouncerateType) {
		if (bouncerateType == null) {
			return;
		}
		storeString(BOUNCE_RATE_TYPE, bouncerateType.toString());
	}

	public static Integer getBouncerateValue() {
		Integer value = getInt(BOUNCE_RATE_VALUE);
		if (value == null) {
			return 1;
		}
		return value;
	}

	public static void setBouncerateValue(String bounceRateValue) {
		if (bounceRateValue.equals("")) {
			return;
		}
		try{
		    setBouncerateValue(Integer.parseInt(bounceRateValue));
		}catch (NumberFormatException e){
        }
	}

	public static void setBouncerateValue(int bounceRateValue) {
		if(bounceRateValue >= 1){
            storeInt(BOUNCE_RATE_VALUE, bounceRateValue);
        }
		else{
            try {
                throw new Exception("Please enter a valid value.");
            } catch (Exception e) {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR,e.getMessage(), ButtonType.OK);
                errorAlert.showAndWait();
                storeInt(BOUNCE_RATE_VALUE, 1);
            }
        }

	}

	public static void storeString(String key, String value) {
		try {
			Database.insertSetting(key, value, Authentication.getCurrentUser());
			Database.commitData();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static String getString(String key) {
		int client_id = Authentication.getCurrentUser();
		PreparedStatement statement;
		try {
			statement = Database.getConnection().prepareStatement("SELECT value FROM settings WHERE key = ? AND client_id = ?");
			statement.setString(1, key);
			statement.setInt(2, client_id);
			ResultSet rs = Database.query(statement);
			if (!rs.next()) {
				return null;
			}
			return rs.getString(1);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void storeInt(String key, int value) {
		storeString(key, Integer.toString(value));
	}

	public static Integer getInt(String key) {
		String s = getString(key);
		if (s == null) {
			return null;
		}
		return Integer.parseInt(s);
	}

	public static void saveFrontendConfig(FrontendConfig frontendConfig) {
		try {
			storeString(FRONTEND_CONFIG_KEY, frontendConfig.serialize());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static FrontendConfig getFrontendConfig() {
		String str = getString(FRONTEND_CONFIG_KEY);
		if (str == null || str.equals("")) {
			return null;
		}
		try {
			return FrontendConfig.deSerialize(str);
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void acceptLegal() {
		acceptLegal(true);
	}

	public static void acceptLegal(boolean accept) {
		storeString(LEGAL, accept ? "Y" : "N");
	}

	public static Boolean acceptedLegal() {
		String string = getString(LEGAL);
		return (string != null && string.equals("Y"));
	}

	public enum BounceRateType {
		PAGES("Pages visited"), TIME("Minutes spent on site");
		public final String label;

		BounceRateType(String label) {
			this.label = label;
		}

		public static List<String> labelValues() {
			return Arrays.stream(values()).map(t -> t.label).collect(Collectors.toList());
		}

		public static BounceRateType valueOfLabel(String label) {
			for (BounceRateType e : values()) {
				if (e.label.equals(label)) {
					return e;
				}
			}
			return null;
		}
	}
}
