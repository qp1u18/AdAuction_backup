package frontend.widget;

import backend.Campaign;
import backend.Common;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

public class ListData extends MetaData {

    private List<String> metricList;

    public ListData(int campaignId, LocalDateTime from, LocalDateTime to, Common.Interval interval, Map<String, EnumSet<? extends Common.Property>> filters, List<String> metricList) {
        super(campaignId, from, to, interval, filters);
        this.metricList = new ArrayList<>(metricList);
    }

    public List<String> getMetricList()  {
        return metricList;
    }


    public Boolean isGraph() { return false; }

    public Map<String, Number> generateData() {
		return getMetricList().stream().collect(toMap(metric1 -> metric1, this::sumData));
	}

	private Number sumData(String metric) {
		ArrayList<Number> metricArrayList = new ArrayList<>(getData(metric).values());
		Float sumValue = 0f;
		for(int i = 0; i < metricArrayList.size();i++) {
			sumValue += metricArrayList.get(i).floatValue();
		}
		return sumValue;
	}

	private Map<String,? extends Number> getData(String metric){
		Campaign c = new Campaign(campaignId);
		switch(metric) {
			case "No. Clicks":
				return c.calClicks(from,to,interval,filters);
			case "No. Bounces":
				return  c.calBounces(from,to,interval,filters);
			case "No. Uniques":
				return   c.calUniques(from,to,interval,filters);
			case "No. Impressions":
				return   c.calImpressions(from,to,interval,filters);
			case "No. Conversions":
				return   c.calConversions(from,to,interval,filters);
			case "Total Cost":
				return   c.calTotalCost(from,to,interval,filters);
			case "CTR":
				return   c.calCTR(from,to,interval,filters);
			case "CPA":
				return   c.calCPA(from,to,interval,filters);
			case "CPC":
				return   c.calCPC(from,to,interval,filters);
			case "CPM":
				return   c.calCPM(from,to,interval,filters);
			case "Bounce Rate":
				return   c.calBounceRate(from,to,interval,filters);
		}
		return null;
	}

}