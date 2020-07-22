package frontend.widget;

import backend.Common;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Map;

public class HistogramData extends GraphData {

    public HistogramData(int campaignId, String metric, LocalDateTime from, LocalDateTime to, Common.Interval interval, Map<String, EnumSet<? extends Common.Property>> filters, String chartColour) {
        super(campaignId, metric, from, to, interval, filters, chartColour);
    }
}
