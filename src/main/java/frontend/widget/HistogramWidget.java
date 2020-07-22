package frontend.widget;

import frontend.CreateXYData;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;

import java.util.ArrayList;
import java.util.HashMap;

public class HistogramWidget extends Widget {
    public HistogramWidget() {
    }

    public HistogramWidget(MetaData n) {
        super(n);
    }

    public HistogramWidget(ArrayList<MetaData> n) {
        super(n);
    }

    @Override
    public Parent create() {
        ObservableList<String> cat = FXCollections.observableArrayList(this.getWidgetData().get(0).getData().keySet());
        BarChart barchart = new BarChart(new CategoryAxis(cat), new NumberAxis());
        barchart.setCategoryGap(0);
        barchart.setBarGap(0);
        for(MetaData n : this.dataList) {
            barchart.getData().add(CreateXYData.createBar(n, new HashMap<>(n.getData())));
        }
        barchart.getXAxis().setLabel("Date");
        barchart.getYAxis().setLabel(this.getWidgetData().get(0).metric);
        barchart.setAnimated(false);
        return barchart;
    }
}
