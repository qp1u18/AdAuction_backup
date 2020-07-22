package frontend;

import backend.Authentication;
import backend.Campaign;
import backend.Common;
import backend.Settings;
import frontend.widget.*;

import java.io.*;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;

public class FrontendConfig implements Serializable {

	private int currentlyEditing = -1;

	private Map<Integer, Widget> widgets;

	public static FrontendConfig instance = null;

	private FrontendConfig() {
		widgets = new HashMap<>();
		for (int i=1; i < 5; i++) {
			widgets.put(i, null);
		}
	}

	public static FrontendConfig getInstance() {
		if (instance == null) {
			instance = Settings.getFrontendConfig();
		}
		if (instance == null) {
			instance = new FrontendConfig();
			createDefault();
		}
		return instance;
	}

	static void reset() {
		instance = null;
	}

	static void saveWidget(Widget widget) {
		if (getInstance().currentlyEditing == -1) {
			System.out.println("SAVE WIDGET ERROR");
			return;
		}
		getInstance().widgets.put(getInstance().currentlyEditing, widget);
		finishEditing();
	}

	static Widget getWidget(Integer index) {
		return getInstance().widgets.get(index);
	}

	static void deleteWidget(int index) {
		getInstance().widgets.put(index, null);
		finishEditing();
	}

	static Map<Integer, Widget> getWidgets() {
		return getInstance().widgets;
	}

	static void startEditing(int index) {
		getInstance().currentlyEditing = index;
	}

	static void finishEditing() {
		getInstance().currentlyEditing = -1;
		save();
	}

	public static FrontendConfig deSerialize(String s ) throws IOException, ClassNotFoundException {
		byte [] data = Base64.getDecoder().decode( s );
		ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data) );
		Object o = ois.readObject();
		ois.close();
		return (FrontendConfig) o;
	}

	public String serialize() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(this);
		oos.close();
		return Base64.getEncoder().encodeToString(baos.toByteArray());
	}

	static void save() {
		Settings.saveFrontendConfig(getInstance());
	}

	static void refreshWidgets() {
		getInstance().widgets.forEach((integer, widget) -> {
			if (widget != null) {
				widget.refresh();
			}
		});
	}

	static int getEditing() {
		return getInstance().currentlyEditing;
	}

	public static void createDefault() {
		if (getDefaultLayoutCampaign() == -1) {
			return;
		}

		Widget widget1 = new GraphWidget();
		widget1.widgetAddData(noFilterMetricData("Bounce Rate"));
		widget1.widgetAddData(noFilterMetricData("CTR"));
		getWidgets().put(1, widget1);

		Widget widget2 = new GraphWidget();
		widget2.widgetAddData(noFilterMetricData("No. Conversions"));
		widget2.widgetAddData(maleMetricData("No. Conversions"));
		widget2.widgetAddData(femaleMetricData("No. Conversions"));
		getWidgets().put(2, widget2);

		Widget widget3 = new HistogramWidget();
		widget3.widgetAddData(noFilterMetricData("CPC"));
		getWidgets().put(3, widget3);

		Widget widget4 = new ListWidget();
		widget4.widgetAddData(listData());
		getWidgets().put(4, widget4);

		save();
	}

	private static GraphData noFilterMetricData(String metric) {
		Map<String, EnumSet<? extends Common.Property>> filters = new LinkedHashMap<>();
		filters.put("gender", EnumSet.noneOf(Common.Gender.class));
		filters.put("income", EnumSet.noneOf(Common.Income.class));
		filters.put("age", EnumSet.noneOf(Common.AgeRange.class));
		filters.put("context",EnumSet.noneOf(Common.Context.class));
		return new GraphData(getDefaultLayoutCampaign(), metric, LocalDateTime.of(2015, 1, 1, 1, 1), LocalDateTime.of(2015, 1, 14, 1, 1), Common.Interval.DAY, filters, null);
	}

	private static GraphData maleMetricData(String metric) {
		Map<String, EnumSet<? extends Common.Property>> filters = new LinkedHashMap<>();
		filters.put("gender", EnumSet.of(Common.Gender.MALE));
		filters.put("income", EnumSet.noneOf(Common.Income.class));
		filters.put("age", EnumSet.noneOf(Common.AgeRange.class));
		filters.put("context",EnumSet.noneOf(Common.Context.class));
		return new GraphData(getDefaultLayoutCampaign(), metric, LocalDateTime.of(2015, 1, 1, 1, 1), LocalDateTime.of(2015, 1, 14, 1, 1), Common.Interval.DAY, filters, null);
	}

	private static GraphData femaleMetricData(String metric) {
		Map<String, EnumSet<? extends Common.Property>> filters = new LinkedHashMap<>();
		filters.put("gender", EnumSet.of(Common.Gender.FEMALE));
		filters.put("income", EnumSet.noneOf(Common.Income.class));
		filters.put("age", EnumSet.noneOf(Common.AgeRange.class));
		filters.put("context",EnumSet.noneOf(Common.Context.class));
		return new GraphData(getDefaultLayoutCampaign(), metric, LocalDateTime.of(2015, 1, 1, 1, 1), LocalDateTime.of(2015, 1, 14, 1, 1), Common.Interval.DAY, filters, null);
	}

	private static ListData listData() {
		Map<String, EnumSet<? extends Common.Property>> filters = new LinkedHashMap<>();
		filters.put("gender", EnumSet.noneOf(Common.Gender.class));
		filters.put("income", EnumSet.noneOf(Common.Income.class));
		filters.put("age", EnumSet.noneOf(Common.AgeRange.class));
		filters.put("context",EnumSet.noneOf(Common.Context.class));
		List<String> metricList = new ArrayList<>();
		metricList.add("No. Impressions");
		metricList.add("No. Clicks");
		metricList.add("Total Cost");
		return new ListData(getDefaultLayoutCampaign(), LocalDateTime.of(2015, 1, 1, 1, 1), LocalDateTime.of(2015, 1, 14, 1, 1), Common.Interval.DAY, filters, metricList);
	}

	static int getDefaultLayoutCampaign() {
		List<Campaign> campaigns;
		try {
			campaigns = Campaign.getCampaigns(Authentication.getCurrentUser());
		} catch (SQLException e) {
			e.printStackTrace();
			return -1;
		}
		if (campaigns.size() == 0) {
			return -1;
		}
		return campaigns.get(0).getId();
	}
}
