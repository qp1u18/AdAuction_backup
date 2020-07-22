package frontend.widget;

import backend.Common;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Map;

public abstract class MetaData implements Serializable {

    public int campaignId;
    public String metric;
    public LocalDateTime from;
    public LocalDateTime to;
    public Common.Interval interval;
    public Map<String, EnumSet<? extends Common.Property>> filters;
    private Map<String, Number> data;

    public MetaData(int campaignId, LocalDateTime from, LocalDateTime to, Common.Interval interval, Map<String, EnumSet<? extends Common.Property>> filters) {
        this.campaignId = campaignId;
        this.from = from;
        this.to = to;
        this.interval = interval;
        this.filters = filters;
    }

    public int getCampaignId(){
        return campaignId;
    }


    public LocalDateTime getFrom(){
        return from;
    }


    public LocalDateTime getTo(){
        return to;
    }


    public Common.Interval getInterval(){
        return interval;
    }


    public Map<String, EnumSet<? extends Common.Property>> getFilters(){
        return filters;
    }


    public Map<String, Number> getData(){
    	if (data == null) {
    		data = generateData();
		}
        return data;
    }

    public Boolean isGraph() {return null;}

    public abstract Map<String, Number> generateData();

    public void refresh() {
    	data = generateData();
	}

	public String getMetric() {
		return metric;
	}
}
