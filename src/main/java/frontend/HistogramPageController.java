package frontend;

import backend.CSVParser;
import frontend.widget.GraphData;
import frontend.widget.HistogramData;
import frontend.widget.HistogramWidget;
import frontend.widget.Widget;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;

import javax.tools.Tool;
import java.net.URL;
import java.util.ResourceBundle;

public class HistogramPageController extends ChartPageController {
    public static Parent graph = null;
    public static Widget staticWidget = null;

    @FXML
    public ComboBox<String> campaignComboBox;
    @FXML
    public ComboBox<String> metricComboBox;
    @FXML
    private DatePicker dateFrom;
    @FXML
    private DatePicker dateTo;
    @FXML
    private Button saveButton;
    @FXML
    private Button loadChartButton;
    @FXML
    private BorderPane chartBorderPane;
    @FXML
    public ListView<String> listViewGender;
    @FXML
    public ListView<String> listViewAge;
    @FXML
    public ListView<String> listViewIncome;
    @FXML
    public ListView<String> listViewContext;
    @FXML
    public ComboBox<String> timeGranularityComboBox;
    @FXML
    public ColorPicker colorPicker;


    @Override
    public void initialize(URL fxmlLocation, ResourceBundle resources) {
        setupInputs();

		loadPrevious();

		setupSaveButton();

		loadChartButtonSetup();

		saveButton.setTooltip(new Tooltip("Save histogram to dashboard."));
        loadChartButton.setTooltip(new Tooltip("Reload the histogram with updated filters."));
    }

	private void loadPrevious() {
		if (FrontendConfig.getWidget(FrontendConfig.getEditing()) != null) {
			widget = FrontendConfig.getWidget(FrontendConfig.getEditing());
			createChart();
			loadLine(0);
		}
		else {
			widget = new HistogramWidget();
			emptyChart();
		}
	}

    protected void loadChartButtonSetup() {
		//Loads all lines from arraylist, creates new chart then adds them all.
		loadChartButton.setOnAction(event -> {
			try {
				HistogramData newData = createConfig();
				widget.widgetAddData(newData);

				Parent barChart = widget.create();

				staticWidget = widget;

				chartBorderPane.setCenter(barChart);
				graph = barChart;
			}
			catch (NullPointerException | CSVParser.ParseError ex){
				Alert errorAlert = new Alert(Alert.AlertType.ERROR, "ERROR: Please enter all parameters.", ButtonType.OK);
				errorAlert.showAndWait();
				ex.printStackTrace();
			} catch (Exception e) {
				Alert errorAlert = new Alert(Alert.AlertType.ERROR, "ERROR: Please enter correct date range.", ButtonType.OK);
				errorAlert.showAndWait();
				e.printStackTrace();
			}
		});
	}


    protected HistogramData createConfig() throws Exception {
		GraphData graphData = super.createConfig();

        //newSeries.getNode().setStyle("CHART_COLOR_" + i + ": " + ((GraphWidget) n).chartColour + " ;");

        return new HistogramData(graphData.campaignId, graphData.metric, graphData.from, graphData.to, graphData.interval, graphData.filters, graphData.chartColour);
    }

    //Formats Color into a string that can be used by CSS.
    private String parseColor(Color color) {
        return String.format( "#%02X%02X%02X",
                (int)( color.getRed() * 255 ),
                (int)( color.getGreen() * 255 ),
                (int)( color.getBlue() * 255 ) );
    }

	public Widget getWidget() {
        return widget;
    }

    public Parent getGraph() {
        return graph;
    }
}