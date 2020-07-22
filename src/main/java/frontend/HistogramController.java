package frontend;

import frontend.widget.MetaData;
import frontend.widget.Widget;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;

import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;

public class HistogramController implements Initializable {
    @FXML
    private BarChart lineChart;
    @FXML
    private CategoryAxis xAxis;
    @FXML
    private NumberAxis yAxis;



    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //Fill in if need be.
    }

    public static BarChart setDataPoint(Widget widget) {
        ObservableList<String> cat = FXCollections.observableArrayList(widget.getWidgetData().get(0).getData().keySet());
        BarChart barChart = new BarChart(new CategoryAxis(cat), new NumberAxis());

        barChart.setCategoryGap(0);
        barChart.setBarGap(0);

        for(MetaData n : widget.dataList) {
            barChart.getData().add(CreateXYData.createLine(n, new HashMap<>(n.getData())));
        }
        barChart.getXAxis().setLabel("Date");
        barChart.getYAxis().setLabel(widget.getWidgetData().get(0).metric);

        return barChart;
    }

}