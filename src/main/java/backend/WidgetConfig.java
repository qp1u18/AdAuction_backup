package backend;

import java.io.*;
import java.time.LocalDateTime;
import java.util.*;

public class WidgetConfig implements Serializable{

	public final boolean isChart;
	public int campaignId;
	public String metric;
	public LocalDateTime from;
	public LocalDateTime to;
	public Common.Interval interval;
	public Map<String, EnumSet<?>> filters;
	public String chartColour;
	public List<String> metricList;

	public WidgetConfig(boolean isChart, int campaignId, String metric, LocalDateTime from, LocalDateTime to, Common.Interval interval, Map<String, EnumSet<?>> filters, String chartColour, List<String> metricList) {
		this.isChart = isChart;
		this.campaignId = campaignId;
		this.metric = metric;
		this.from = from;
		this.to = to;
		this.interval = interval;
		this.filters = filters;
		this.chartColour = chartColour;
		if (metricList != null) {
			this.metricList = new ArrayList<>(metricList);
		}
	}

	public static WidgetConfig deSerialize(String s ) throws IOException, ClassNotFoundException {
		byte [] data = Base64.getDecoder().decode( s );
		ObjectInputStream ois = new ObjectInputStream(
				new ByteArrayInputStream(  data ) );
		Object o  = ois.readObject();
		ois.close();
		return (WidgetConfig) o;
	}

	public String serialize() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream( baos );
		oos.writeObject( this );
		oos.close();
		return Base64.getEncoder().encodeToString(baos.toByteArray());
	}
}
