package frontend;

import backend.Authentication;
import backend.CSVParser;
import backend.Campaign;
import backend.Common;
import frontend.widget.GraphData;
import frontend.widget.GraphWidget;
import frontend.widget.MetaData;
import frontend.widget.Widget;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;

public class ChartPageController implements Initializable {
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
    @FXML
	public ComboBox<String> selectedLine;
    @FXML
	public Button deleteLine;

    protected Widget widget;

    @Override
    public void initialize(URL fxmlLocation, ResourceBundle resources) {
        setupInputs();

		loadPrevious();

        setupSaveButton();

        setupLineEditing();

        loadChartButtonSetup();

        saveButton.setTooltip(new Tooltip("Save graph to dashboard."));
        deleteLine.setTooltip(new Tooltip("Delete a selected line."));
        loadChartButton.setTooltip(new Tooltip("Reload the chart with updated filters."));


    }

	private void loadChartButtonSetup() {
		//Loads all lines from arraylist, creates new chart then adds them all.
		loadChartButton.setOnAction(event -> {
			try {
				GraphData newLine = createConfig();
				if (selectedLine.getValue().equals("New line")) {
					widget.widgetAddData(newLine);
					selectedLine.getItems().add(Integer.toString(selectedLine.getItems().size()));
				}
				else {
					widget.widgetSetData(Integer.parseInt(selectedLine.getValue()) - 1, newLine);
				}

				createChart();
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

	private void setupLineEditing() {
		deleteLine.setOnAction(event -> {
			if (selectedLine.getValue().equals("New line")) {
				return;
			}
			widget.widgetDeleteData(selectedLine.getItems().indexOf(selectedLine.getValue()) - 1);
			selectedLine.getItems().remove(selectedLine.getValue());
			createChart();
		});

		selectedLine.setOnAction(actionEvent -> {
			if (selectedLine.getValue().equals("New line")) {
				return;
			}
			loadLine(selectedLine.getItems().indexOf(selectedLine.getValue()) - 1);
		});
	}

	protected void setupSaveButton() {
		saveButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent actionEvent) {
				Parent root;
				try {
					FrontendConfig.saveWidget(widget);

					root = FXMLLoader.load(Objects.requireNonNull(getClass().getClassLoader().getResource("Home_Page.fxml")));
					Stage homeStage = new Stage();

					homeStage.setTitle("Advertisement System");
					Scene scene = new Scene(root, 1300, 800);
					scene.getStylesheets().add(ManageCSS.getFontSizeCSS());
					homeStage.setScene(scene);
					homeStage.setResizable(false);
					homeStage.show();

					((Node)(actionEvent.getSource())).getScene().getWindow().hide();
				}
				catch(IOException e){
					e.printStackTrace();
				} catch (Exception e) {
					Alert errorAlert = new Alert(Alert.AlertType.ERROR, e.getMessage(), ButtonType.OK);
					errorAlert.showAndWait();
				}
			}
		});
	}

	private void loadPrevious() {
		if (FrontendConfig.getWidget(FrontendConfig.getEditing()) != null) {
			widget = FrontendConfig.getWidget(FrontendConfig.getEditing());
			createChart();
		}
		else {
			widget = new GraphWidget();
			emptyChart();
		}

		selectedLine.getItems().add("New line");
		for (int i=1; i <= widget.getWidgetData().size(); i++) {
			selectedLine.getItems().add(Integer.toString(i));
		}
		selectedLine.setValue(selectedLine.getItems().get(0));
	}

    protected void setupInputs() {
		metricComboBox.getItems().setAll("No. Impressions", "No. Clicks", "No. Uniques", "No. Bounces", "No. Conversions", "Total Cost", "CTR", "CPA", "CPC", "CPM", "Bounce Rate");
		metricComboBox.setPromptText(" Select Metric");

		dateFrom.setPromptText(" From");
		dateTo.setPromptText(" To");

		final ObservableList<String> interval = FXCollections.observableArrayList(Common.allValues(Common.Interval.class));
		timeGranularityComboBox.setItems(interval);
		timeGranularityComboBox.setPromptText(" Select Time Granularity");

		final ObservableList<String> gender = FXCollections.observableArrayList(Common.allValues(Common.Gender.class));
		listViewGender.setItems(gender);
		listViewGender.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

		final ObservableList<String> age = FXCollections.observableArrayList(Common.allValues(Common.AgeRange.class));
		listViewAge.setItems(age);
		listViewAge.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

		final ObservableList<String> context = FXCollections.observableArrayList(Common.allValues(Common.Context.class));
		listViewContext.setItems(context);
		listViewContext.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

		final ObservableList<String> income = FXCollections.observableArrayList(Common.allValues(Common.Income.class));
		listViewIncome.setItems(income);
		listViewIncome.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

		campaignComboBox.setItems(Campaign.campaignNameList(Authentication.getCurrentUser()));
		campaignComboBox.setValue(campaignComboBox.getItems().get(0));


		colorPicker.setValue(Color.ORANGE);
	}

    protected void createChart() {
		Parent chart = widget.create();
		chartBorderPane.setCenter(chart);
	}

	protected void loadLine(int line) {
		MetaData graphData;
    	try {
			graphData = widget.getWidgetData().get(line);
		}
    	catch (IndexOutOfBoundsException e) {
    		e.printStackTrace();
    		return;
		}

		Optional<String> campaignItem = campaignComboBox.getItems().stream().filter(s -> s.equals(new Campaign(graphData.campaignId).getName())).findFirst();
		campaignItem.ifPresent(s -> campaignComboBox.setValue(s));

		Optional<String> metricItem = metricComboBox.getItems().stream().filter(s -> s.equals(((GraphData) graphData).metric)).findFirst();
		metricItem.ifPresent(s -> metricComboBox.setValue(s));

    	dateFrom.setValue(graphData.from.toLocalDate());
    	dateTo.setValue(graphData.to.toLocalDate());
    	timeGranularityComboBox.setValue(graphData.interval.getString());

    	selectItems(listViewGender, graphData, "gender");
    	selectItems(listViewAge, graphData, "age");
    	selectItems(listViewContext, graphData, "context");
    	selectItems(listViewIncome, graphData, "income");
	}

	private void selectItems(ListView<String> listView, MetaData graphData, String key) {
    	listView.getSelectionModel().clearSelection();
		EnumSet<? extends Common.Property> items = graphData.filters.get(key);
		items.forEach(item -> {
			Optional<String> listItem = listView.getItems().stream().filter(s -> s.equals(item.getString())).findFirst();
			listItem.ifPresent(s -> listView.getSelectionModel().select(s));
		});

	}

    protected GraphData createConfig() throws Exception {

        String metric = metricComboBox.getValue();

		LocalDateTime from = LocalDateTime.of(dateFrom.getValue().getYear(), dateFrom.getValue().getMonthValue(), dateFrom.getValue().getDayOfMonth(), 0, 0);
		LocalDateTime to = LocalDateTime.of(dateTo.getValue().getYear(), dateTo.getValue().getMonthValue(), dateTo.getValue().getDayOfMonth(), 0, 0);
		if(from.isAfter(to))  throw new Exception("To date must be before From date");

		Common.Interval interval = CSVParser.parseInterval(timeGranularityComboBox.getValue());

		EnumSet<Common.Gender> filter1 = EnumSet.noneOf(Common.Gender.class);
		EnumSet<Common.Income> filter2 = EnumSet.noneOf(Common.Income.class);
		EnumSet<Common.AgeRange> filter3 = EnumSet.noneOf(Common.AgeRange.class);
		EnumSet<Common.Context> filter4 = EnumSet.noneOf(Common.Context.class);
		try {
			for(String s: listViewGender.getSelectionModel().getSelectedItems()) {
				filter1.add(CSVParser.parseGender(s));
			}
			for(String s: listViewIncome.getSelectionModel().getSelectedItems()) {
				filter2.add(CSVParser.parseIncome(s));
			}
			for(String s: listViewAge.getSelectionModel().getSelectedItems()) {
				filter3.add(CSVParser.parseAge(s));
			}
			for(String s: listViewContext.getSelectionModel().getSelectedItems()) {
				filter4.add(CSVParser.parseContext(s));
			}
		} catch (CSVParser.ParseError parseError) {
			parseError.printStackTrace();
		}

		Map<String, EnumSet<? extends Common.Property>> filters = new LinkedHashMap<>();
		filters.put("gender", filter1);
		filters.put("income", filter2);
		filters.put("age", filter3);
		filters.put("context",filter4);

        int campaignId = 1;
        try {
            campaignId = Campaign.getFromName(campaignComboBox.getValue()).getId();
        } catch (SQLException e) {
            e.printStackTrace();
        }

		String chartColor = parseColor(colorPicker.getValue());

        //newSeries.getNode().setStyle("CHART_COLOR_" + i + ": " + ((GraphWidget) n).chartColour + " ;");

		return new GraphData(campaignId, metric, from, to, interval, filters, chartColor);
	}

    protected void emptyChart(){
        try{
			chartBorderPane.setCenter(FXMLLoader.load(getClass().getClassLoader().getResource("Chart.fxml")));
		}
        catch(IOException e){
            e.printStackTrace();
        }
    }

    //Formats Color into a string that can be used by CSS.
    private String parseColor(Color color) {
        return String.format( "#%02X%02X%02X",
                (int)( color.getRed() * 255 ),
                (int)( color.getGreen() * 255 ),
                (int)( color.getBlue() * 255 ) );
    }
}