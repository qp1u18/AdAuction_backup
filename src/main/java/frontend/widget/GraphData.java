package frontend.widget;

import backend.Campaign;
import backend.Common;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Map;

public class GraphData extends MetaData {

    public String chartColour;

    public GraphData(int campaignId, String metric, LocalDateTime from, LocalDateTime to, Common.Interval interval, Map<String, EnumSet<? extends Common.Property>> filters, String chartColour) {
        super(campaignId, from, to, interval, filters);
        this.metric = metric;
        this.chartColour = chartColour;
    }

    public String getChartColour()  {
        return chartColour;
    }

    public Boolean isGraph() { return true; }

	@Override
	public Map<String, Number> generateData() {
		return (Map<String, Number>) genData();
	}

	//Returns HashMap<String, Number> for the data of the line.
	private Map<String, ? extends Number> genData() {
		Campaign c = new Campaign(campaignId);
		switch(metric) {
			case "No. Clicks":
				return c.calClicks(from, to, interval, filters);
			case "No. Uniques":
				return c.calUniques(from, to, interval, filters);
			case "No. Bounces":
				return c.calBounces(from, to, interval, filters);
			case "No. Impressions":
				return c.calImpressions(from, to, interval, filters);
			case "No. Conversions":
				return c.calConversions(from, to, interval, filters);
			case "Total Cost":
				return c.calTotalCost(from, to, interval, filters);
			case "CTR":
				return c.calCTR(from, to, interval, filters);
			case "CPA":
				return c.calCPA(from, to, interval, filters);
			case "CPC":
				return c.calCPC(from, to, interval, filters);
			case "CPM":
				return c.calCPM(from, to, interval, filters);
			case "Bounce Rate":
				return c.calBounceRate(from, to, interval, filters);
			default:
				return null;
		}
	}

}
