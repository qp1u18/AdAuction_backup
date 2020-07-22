package backend;

import frontend.widget.GraphData;
import frontend.widget.MetaData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SettingsTest {

	private Connection connection;

	@BeforeEach
	void setUp() throws SQLException, Authentication.AuthenticationException {
		Database.createTestConnection();
		connection = Database.getConnection();
		Authentication.login("admin", "admin");
	}

	@AfterEach
	void tearDown() throws SQLException {
		connection.close();
		File f = new File("test_database.db");
		f.delete();
	}

	@Test
	void saveString() {
		Settings.storeString("test", "tst");
		assertEquals(Settings.getString("test"), "tst");
	}

	@Test
	void saveInt() {
		Settings.storeInt("testint", 67);
		assertEquals((int) Settings.getInt("testint"), 67);
	}

	@Test
	void legal() {
		assertFalse(Settings.acceptedLegal());
		Settings.acceptLegal();
		assertTrue(Settings.acceptedLegal());
	}

	@Test
	void saveLayout() {
		LocalDateTime to = LocalDateTime.of(1, 2, 3, 4, 5);
		LocalDateTime from = LocalDateTime.of(1, 2, 3, 4, 6);
		Map<String, EnumSet<? extends Common.Property>> filters = new HashMap<>();
		filters.put("filt1", EnumSet.of(Common.AgeRange.F25T34));
		HashMap<String,Number> data = new HashMap<String,Number>() {{put("13-01-2017",1);put("14-01-2017",2);put("15-01-2017",3);put("16-01-2017",4);put("17-01-2017",5);}};

		GraphData widget = new GraphData(1, "metric", from, to, Common.Interval.HOUR, filters, "fff");
		ArrayList<MetaData> metaDataList = new ArrayList<>();
		metaDataList.add(widget);
//		Settings.saveChartLayout(2, new Widget(metaDataList));
//		Widget chartLayout = Settings.getChartLayout(2);
//		assertEquals(chartLayout.getWidgetData().get(0).campaignId, frontend.widget.campaignId);
//		assertEquals(chartLayout.getWidgetData().get(0).metric, frontend.widget.metric);
//		assertEquals(chartLayout.getWidgetData().get(0).metric, frontend.widget.metric);
//		assertEquals(chartLayout.getWidgetData().get(0).to, frontend.widget.to);
//		assertEquals(chartLayout.getWidgetData().get(0).from, frontend.widget.from);
//		assertEquals(chartLayout.getWidgetData().get(0).interval, frontend.widget.interval);
//		//assertEquals(chartLayout.getWidgetData().get(0).chartColour, frontend.widget.chartColour);
//		assertEquals(chartLayout.getWidgetData().get(0).filters, frontend.widget.filters);
//		Settings.deleteChartLayout(2);
//		assertNull(Settings.getChartLayout(2));
	}

}