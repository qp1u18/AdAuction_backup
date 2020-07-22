package frontend;

import backend.Common;
import frontend.widget.MetaData;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Cursor;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CreateXYData {

    public static XYChart.Series createLine(MetaData metaData, HashMap<String, Number> inpData) {
        return addData(createLegend(metaData), inpData);
    }

	private static String createLegend(MetaData metaData) {
		StringBuilder stringBuilder = new StringBuilder(metaData.getMetric());
		List<? extends Common.Property> filters = metaData.filters.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
		String output = stringBuilder.toString();
		if (filters.size() > 0) {
			stringBuilder.append(": ");
			filters.forEach(filter -> stringBuilder.append(String.format("%s, ", filter.getString())));
			output = stringBuilder.substring(0, stringBuilder.length() - 2);
		}
		return output;
	}

    public static XYChart.Series addData(String chartName, HashMap<String, Number> inpData) {
        return new XYChart.Series(chartName, plot(inpData));
    }

    public static ObservableList<XYChart.Data<String, Number>> plot(Map<String,Number> inpData) {
        final ObservableList<XYChart.Data<String, Number>> dataset = FXCollections.observableArrayList();
        for(String key : inpData.keySet()) {
            final XYChart.Data<String, Number> data = new XYChart.Data<>(key, inpData.get(key));
            data.setNode(new HoveredThresholdNode(inpData.get(key).floatValue()));
            dataset.add(data);
        }
        return dataset;
    }

    static class HoveredThresholdNode extends StackPane {

        HoveredThresholdNode(float value) {
            setPrefSize(15, 15);
            final Label label = createDataThresholdLabel(value);

            setOnMouseEntered(mouseEvent -> {
                getChildren().setAll(label);
                setCursor(Cursor.NONE);
                toFront();
            });
            setOnMouseExited(mouseEvent -> {
                getChildren().clear();
                setCursor(Cursor.CROSSHAIR);
            });
        }

        private Label createDataThresholdLabel(float value) {
            final Label label = new Label(" " + value + " ");
            label.getStyleClass().addAll("default-color0", "chart-line-symbol", "chart-series-line");
            label.setStyle("-fx-font-size: 20; -fx-font-weight: bold;");


            label.setMinSize(Label.USE_PREF_SIZE, Label.USE_PREF_SIZE);
            return label;
        }
    }

    public static XYChart.Series createBar(MetaData metaData, HashMap<String, Number> inpData) {
        return addBarData(createLegend(metaData), inpData);
    }

    public static XYChart.Series addBarData(String chartName, HashMap<String,Number> inpData) {
        return new XYChart.Series(chartName, plotHisto(inpData));
    }

    public static ObservableList<XYChart.Data<String, Number>> plotHisto(Map<String,Number> inpData) {
        final ObservableList<XYChart.Data<String, Number>> dataset = FXCollections.observableArrayList();
        for (String key : inpData.keySet()) {
            final XYChart.Data<String, Number> data = new XYChart.Data<>(key, inpData.get(key));
            dataset.add(data);
        }
        return dataset;
    }
}
