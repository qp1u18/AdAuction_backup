package frontend;

import backend.Authentication;
import backend.CSVParser;
import backend.Campaign;
import backend.Common;
import frontend.widget.ListData;
import frontend.widget.ListWidget;
import frontend.widget.MetaData;
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
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;

public class ListPageController implements Initializable {
	@FXML
	public ComboBox<String> campaignComboBox;
	@FXML
	public ListView<String> listViewGender;
	@FXML
	public ListView<String> listViewAge;
	@FXML
	public ListView<String> listViewIncome;
	@FXML
	public ListView<String> listViewContext;
	@FXML
	public ListView<String> listViewMetric;
	public ListWidget widget;
	@FXML
	private DatePicker dateFrom;
	@FXML
	private DatePicker dateTo;
	@FXML
	private Button saveButton;
	@FXML
	private Button loadListButton;
	@FXML
	private BorderPane chartBorderPane;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		campaignComboBox.setItems(Campaign.campaignNameList(Authentication.getCurrentUser()));
		campaignComboBox.setValue(campaignComboBox.getItems().get(0));

		dateFrom.setPromptText(" From");
		dateTo.setPromptText(" To");

		saveButton.setTooltip(new Tooltip("Save list to dashboard."));
		loadListButton.setTooltip(new Tooltip("Reload the chart with updated filters."));

		final ObservableList<String> gender = FXCollections.observableArrayList("Male", "Female");
		listViewGender.setItems(gender);
		listViewGender.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

		final ObservableList<String> age = FXCollections.observableArrayList("<25", "25-34", "35-44", "45-54", ">54");
		listViewAge.setItems(age);
		listViewAge.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

		final ObservableList<String> context = FXCollections.observableArrayList("Blog", "News", "Shopping", "Social Media", "Hobbies", "Travel");
		listViewContext.setItems(context);
		listViewContext.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

		final ObservableList<String> income = FXCollections.observableArrayList("Low", "Medium", "High");
		listViewIncome.setItems(income);
		listViewIncome.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

		final ObservableList<String> metric = FXCollections.observableArrayList("No. Impressions", "No. Clicks", "No. Uniques", "No. Bounces", "No. Conversions", "Total Cost", "CTR", "CPA", "CPC", "CPM", "Bounce Rate");
		listViewMetric.setItems(metric);
		listViewMetric.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

		loadPrevious();

		saveButton.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent actionEvent) {
				Parent root;
				try {
					loadListButton.fire();

					FrontendConfig.saveWidget(widget);

					root = FXMLLoader.load(Objects.requireNonNull(getClass().getClassLoader().getResource("Home_Page.fxml")));
					Stage homeStage = new Stage();

					homeStage.setTitle("Advertisement System");
					Scene scene = new Scene(root, 1300, 800);
					scene.getStylesheets().add(ManageCSS.getFontSizeCSS());
					homeStage.setScene(scene);
					homeStage.setResizable(false);
					homeStage.show();

					((Node) (actionEvent.getSource())).getScene().getWindow().hide();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (Exception e) {
					Alert errorAlert = new Alert(Alert.AlertType.ERROR, e.getMessage(), ButtonType.OK);
					errorAlert.showAndWait();
				}
			}
		});

		loadListButton.setOnAction(event -> {
			try {
				ListData config = createConfig();
				if(widget.getWidgetData().isEmpty()){
                    widget.widgetAddData(config);
                }
				else{
                    widget.widgetSetData(0, config);
                }
				createChart();

			} catch (NullPointerException ex) {
				Alert errorAlert = new Alert(Alert.AlertType.ERROR, "ERROR: Please enter all parameters.", ButtonType.OK);
				errorAlert.showAndWait();
			}
		});
	}

	private ListData createConfig() {
		int campaignId = 1;
		try {
			campaignId = Campaign.getFromName(campaignComboBox.getValue()).getId();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		LocalDateTime from = LocalDateTime.of(dateFrom.getValue().getYear(), dateFrom.getValue().getMonthValue(), dateFrom.getValue().getDayOfMonth(), 0, 0);
		LocalDateTime to = LocalDateTime.of(dateTo.getValue().getYear(), dateTo.getValue().getMonthValue(), dateTo.getValue().getDayOfMonth(), 0, 0);

		ObservableList<String> metricList = listViewMetric.getSelectionModel().getSelectedItems();

		EnumSet<Common.Gender> filter1 = EnumSet.noneOf(Common.Gender.class);
		EnumSet<Common.Income> filter2 = EnumSet.noneOf(Common.Income.class);
		EnumSet<Common.AgeRange> filter3 = EnumSet.noneOf(Common.AgeRange.class);
		EnumSet<Common.Context> filter4 = EnumSet.noneOf(Common.Context.class);
		try {
			for (String s : listViewGender.getSelectionModel().getSelectedItems()) {
				filter1.add(CSVParser.parseGender(s));
			}
			for (String s : listViewIncome.getSelectionModel().getSelectedItems()) {
				filter2.add(CSVParser.parseIncome(s));
			}
			for (String s : listViewAge.getSelectionModel().getSelectedItems()) {
				filter3.add(CSVParser.parseAge(s));
			}
			for (String s : listViewContext.getSelectionModel().getSelectedItems()) {
				filter4.add(CSVParser.parseContext(s));
			}
		} catch (CSVParser.ParseError parseError) {
			parseError.printStackTrace();
		}

		Map<String, EnumSet<? extends Common.Property>> filters = new LinkedHashMap<>();
		filters.put("gender", filter1);
		filters.put("income", filter2);
		filters.put("age", filter3);
		filters.put("context", filter4);

		Common.Interval interval = Common.Interval.DAY;

		return new ListData(campaignId, from, to, interval, filters, metricList);
	}

	private void loadPrevious() {
		if (FrontendConfig.getWidget(FrontendConfig.getEditing()) != null) {
			widget = (ListWidget) FrontendConfig.getWidget(FrontendConfig.getEditing());
			createChart();
			loadLine();
		} else {
			widget = new ListWidget();
			try {
				chartBorderPane.setCenter(FXMLLoader.load(getClass().getClassLoader().getResource("List.fxml")));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void createChart() {
		Parent table = widget.create();
		chartBorderPane.setCenter(table);
	}

	protected void loadLine() {
		ListData listData;
		try {
			listData = (ListData) widget.getWidgetData().get(0);
		}
		catch (IndexOutOfBoundsException e) {
			e.printStackTrace();
			return;
		}

		Optional<String> campaignItem = campaignComboBox.getItems().stream().filter(s -> s.equals(new Campaign(listData.campaignId).getName())).findFirst();
		campaignItem.ifPresent(s -> campaignComboBox.setValue(s));

		dateFrom.setValue(listData.from.toLocalDate());
		dateTo.setValue(listData.to.toLocalDate());

		selectItems(listViewGender, listData, "gender");
		selectItems(listViewAge, listData, "age");
		selectItems(listViewContext, listData, "context");
		selectItems(listViewIncome, listData, "income");

		listData.getMetricList().forEach(metric -> {
			Optional<String> listItem = listViewMetric.getItems().stream().filter(s -> s.equals(metric)).findFirst();
			listItem.ifPresent(s -> listViewMetric.getSelectionModel().select(s));
		});
	}

	private void selectItems(ListView<String> listView, MetaData graphData, String key) {
		listView.getSelectionModel().clearSelection();
		EnumSet<? extends Common.Property> items = graphData.filters.get(key);
		items.forEach(item -> {
			Optional<String> listItem = listView.getItems().stream().filter(s -> s.equals(item.getString())).findFirst();
			listItem.ifPresent(s -> listView.getSelectionModel().select(s));
		});

	}

}
