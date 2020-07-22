package frontend;

import frontend.widget.MetaData;
import frontend.widget.Widget;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;

import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;

public class ChartController implements Initializable {
    @FXML
    private LineChart lineChart;
    @FXML
    private CategoryAxis xAxis;
    @FXML
    private NumberAxis yAxis;



    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //Fill in if need be.
    }

    public static LineChart setDataPoint(Widget widget) {
        ObservableList<String> cat = FXCollections.observableArrayList(widget.getWidgetData().get(0).getData().keySet());
        LineChart linechart = new LineChart(new CategoryAxis(cat), new NumberAxis());


        for(MetaData n : widget.dataList) {
            linechart.getData().add(CreateXYData.createLine(n, new HashMap<>(n.getData())));
        }
        linechart.getXAxis().setLabel("Date");
        linechart.getYAxis().setLabel(widget.getWidgetData().get(0).metric);

        return linechart;
    }

}