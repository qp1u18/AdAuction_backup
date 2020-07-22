package frontend.widget;

import frontend.CreateXYData;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;

import java.util.ArrayList;
import java.util.HashMap;

public class GraphWidget extends Widget {
	public GraphWidget() {
	}

	public GraphWidget(MetaData n) {
		super(n);
	}

	public GraphWidget(ArrayList<MetaData> n) {
		super(n);
	}

	@Override
	public Parent create() {
		ObservableList<String> cat;
		try {
			cat = FXCollections.observableArrayList(this.getWidgetData().get(0).getData().keySet());
		}
		catch (IndexOutOfBoundsException e) {
			return new LineChart(new CategoryAxis(), new NumberAxis());
		}
		LineChart linechart = new LineChart(new CategoryAxis(cat), new NumberAxis());
		for(MetaData n : this.dataList) {
			linechart.getData().add(CreateXYData.createLine(n, new HashMap<>(n.getData())));
		}
		linechart.getXAxis().setLabel("Date");
		linechart.getYAxis().setLabel(this.getWidgetData().get(0).metric);
        linechart.setAnimated(false);

		return linechart;
	}
}
