package frontend.widget;

public class DataPoint {

    private String metric;
    private Number value;

    public DataPoint(String metric, Number value) {
        this.metric = metric;
        this.value = value;

    }

    public String getMetric() {
        return metric;
    }

    public Number getValue() {
        return value;
    }

}