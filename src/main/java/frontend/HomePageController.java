package frontend;

import backend.Authentication;
import backend.CSVParser;
import backend.Campaign;
import backend.Settings;
import frontend.widget.GraphWidget;
import frontend.widget.HistogramWidget;
import frontend.widget.ListWidget;
import frontend.widget.Widget;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.print.*;
import javafx.scene.*;
import javafx.scene.control.Button;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.transform.Scale;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import javax.tools.Tool;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.List;
import java.util.*;
import java.util.function.Consumer;


public class HomePageController implements Initializable {

    @FXML
    private Button addButton1;
    @FXML
    private Button addButton2;
    @FXML
    private Button addButton3;
    @FXML
    private Button addButton4;
    @FXML
    private Button editButton1;
    @FXML
    private Button editButton2;
    @FXML
    private Button editButton3;
    @FXML
    private Button editButton4;
    @FXML
    private Button deleteButton1;
    @FXML
    private Button deleteButton2;
    @FXML
    private Button deleteButton3;
    @FXML
    private Button deleteButton4;
    @FXML
    private Button importCSVButton;
    @FXML
    private Button logoutButton;
	@FXML
	private Button settingsButton;
    @FXML
    private BorderPane borderPane1;
    @FXML
    private BorderPane borderPane2;
    @FXML
    private BorderPane borderPane3;
    @FXML
    private BorderPane borderPane4;
    @FXML
	private ComboBox<String> importCampaignId;
    @FXML
	private Button manageCampaigns;
    @FXML
    private MenuBar menu;
    @FXML
    private MenuItem exportGraph1;
    @FXML
    private MenuItem exportGraph2;
    @FXML
    private MenuItem exportGraph3;
    @FXML
    private MenuItem exportGraph4;
    @FXML
    private MenuItem printGraph1;
    @FXML
    private MenuItem printGraph2;
    @FXML
    private MenuItem printGraph3;
    @FXML
    private MenuItem printGraph4;

    public static File selectedFile;
    private FileChooser fileChooser;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setAddButton(addButton1);
        setAddButton(addButton2);
        setAddButton(addButton3);
        setAddButton(addButton4);
        setEditButton(editButton1);
        setEditButton(editButton2);
        setEditButton(editButton3);
        setEditButton(editButton4);
        setDeleteButton(deleteButton1);
        setDeleteButton(deleteButton2);
        setDeleteButton(deleteButton3);
        setDeleteButton(deleteButton4);
        fileChooser = new FileChooser();

        FrontendConfig.getWidgets().forEach(this::setWidget);

        importCSVButton.setTooltip(new Tooltip("Import a CSV file into the system."));
        manageCampaigns.setTooltip(new Tooltip("Add, edit and delete campaigns in the system."));
        settingsButton.setTooltip((new Tooltip("Edit font and bounce rates.")));
        logoutButton.setTooltip((new Tooltip("Logout of the system.")));

        addButton1.setTooltip(new Tooltip("Add graphs, lists or histograms to the dashboard."));
        addButton2.setTooltip(new Tooltip("Add graphs, lists or histograms to the dashboard."));
        addButton3.setTooltip(new Tooltip("Add graphs, lists or histograms to the dashboard."));
        addButton4.setTooltip(new Tooltip("Add graphs, lists or histograms to the dashboard."));

        editButton1.setTooltip(new Tooltip("Edit the graph, list or histogram."));
        editButton2.setTooltip(new Tooltip("Edit the graph, list or histogram."));
        editButton3.setTooltip(new Tooltip("Edit the graph, list or histogram."));
        editButton4.setTooltip(new Tooltip("Edit the graph, list or histogram."));

        deleteButton1.setTooltip(new Tooltip("Delete the graph, list or histogram."));
        deleteButton2.setTooltip(new Tooltip("Delete the graph, list or histogram."));
        deleteButton3.setTooltip(new Tooltip("Delete the graph, list or histogram."));
        deleteButton4.setTooltip(new Tooltip("Delete the graph, list or histogram."));



        importCampaignId.setItems(Campaign.campaignNameList(Authentication.getCurrentUser()));
		if (importCampaignId.getItems().size() > 0) {
			importCampaignId.setValue(importCampaignId.getItems().get(0));
		}

		manageCampaigns.setOnAction(event -> {
			ManageCampaignsController.popen(this);
			refreshScreen((Node)(event.getSource()));
		});

        importCSVButton.setOnAction(e -> {
            HashMap<String, String> agree = new HashMap<>();
            Settings.acceptLegal(false); // REMOVE AFTER PRESENTATION
            if (!Settings.acceptedLegal()) {
				agree.put("Test", "Not Confirmed");
				Alert legalAlert = createAlertWithOptOut(Alert.AlertType.CONFIRMATION, "Confirm", "Please confirm to accepting the Legal Disclaimer:", "<Insert Text>", "I accept that I have read the Legal Disclaimer.", param -> agree.put("Test", param ? "Confirmed" : "Not Confirmed"), ButtonType.YES, ButtonType.CANCEL);
				legalAlert.showAndWait();
				if(legalAlert.getResult().getText().equals("是") && agree.get("Test").equals("Confirmed")) {
					Settings.acceptLegal();
				}
			}

            if (importCampaignId.getValue() == null) {
				Alert errorAlert = new Alert(Alert.AlertType.ERROR, "Please select a campaign to import data into", ButtonType.OK);
				errorAlert.showAndWait();
				return;
			}

            if(Settings.acceptedLegal()) {
                selectedFile = fileChooser.showOpenDialog(null);
                if (selectedFile == null) {
                    return;
                }
                try {
					int campaignId = 1;
					try {
						campaignId = Campaign.getFromName(importCampaignId.getValue()).getId();
					} catch (SQLException e2) {
						e2.printStackTrace();
					}
                    CSVParser.parseCSV(selectedFile.toString(), campaignId);
                    Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION, "CSV File Loaded Successfully.", ButtonType.OK);
                    confirmAlert.showAndWait();
                } catch (CSVParser.ParseError ex) {
                    ex.printStackTrace();
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR, "CSV File Not Loaded - " + ex.getMessage(), ButtonType.OK);
                    errorAlert.showAndWait();
                }
            }
            else {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR, "Please accept the Legal Disclaimer to import a CSV file", ButtonType.OK);
                errorAlert.showAndWait();
            }
        });

        logoutButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
            	FrontendConfig.reset();
                Parent root;
                try{
                    root = FXMLLoader.load(getClass().getClassLoader().getResource("Login.fxml"));
                    Stage loginStage = new Stage();
                    loginStage.setTitle("Login");
                    Scene scene = new Scene(root, 600, 400);
                    scene.getStylesheets().add(ManageCSS.getFontSizeCSS());
                    loginStage.setScene(scene);
                    loginStage.setResizable(false);
                    loginStage.show();
                    ((Node)(event.getSource())).getScene().getWindow().hide();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        settingsButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent actionEvent) {
				try {
					Stage popup = new Stage();

					popup.initModality(Modality.APPLICATION_MODAL);
					popup.setTitle("Settings");

					Parent layout = FXMLLoader.load(getClass().getClassLoader().getResource("Settings.fxml"));
					Scene scene = new Scene(layout, 600, 400);

					popup.setScene(scene);

					popup.showAndWait();

					FrontendConfig.refreshWidgets();

					refreshScreen((Node)(actionEvent.getSource()));
				}catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
    }

    private void refreshScreen(Node node) {
		try{
			Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getClassLoader().getResource("Home_Page.fxml")));
			Stage newHomeStage = new Stage();
			newHomeStage.setTitle("Advertisement System");
			Scene homeScene = new Scene(root, 1300, 800);

			homeScene.getStylesheets().add(ManageCSS.getFontStyleCSS());
			if(!homeScene.getStylesheets().contains(ManageCSS.getFontSizeCSS())) homeScene.getStylesheets().add(ManageCSS.getFontSizeCSS());

			newHomeStage.setScene(homeScene);
			newHomeStage.setResizable(false);
			newHomeStage.show();
			node.getScene().getWindow().hide();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void setWidget(int index, Widget widget) {
    	BorderPane borderPane;
    	Button addButton;
    	Button editButton;
    	Button deleteButton;
    	switch (index) {
			case 1:
				borderPane = borderPane1;
				addButton = addButton1;
				editButton = editButton1;
				deleteButton = deleteButton1;
				break;
			case 2:
				borderPane = borderPane2;
				addButton = addButton2;
				editButton = editButton2;
				deleteButton = deleteButton2;
				break;
			case 3:
				borderPane = borderPane3;
				addButton = addButton3;
				editButton = editButton3;
				deleteButton = deleteButton3;
				break;
			case 4:
				borderPane = borderPane4;
				addButton = addButton4;
				editButton = editButton4;
				deleteButton = deleteButton4;
				break;
			default:
				System.out.println("Invalid index " + index);
				return;
		}

		if (widget != null) {
			borderPane.setCenter(widget.create());
		}
		else {
			borderPane.setCenter(null);
		}

		addButton.setVisible(widget == null);
		editButton.setVisible(widget != null);
		deleteButton.setVisible(widget != null);
	}

    private int indexFromButton(Button button) {
    	switch (button.getId()) {
    		case "addButton1":
			case "editButton1":
			case "deleteButton1":
				return 1;
			case "addButton2":
			case "editButton2":
			case "deleteButton2":
				return 2;
			case "addButton3":
			case "editButton3":
			case "deleteButton3":
				return 3;
			case "addButton4":
			case "editButton4":
			case "deleteButton4":
				return 4;
		}
		return -1;
	}

    private void setAddButton(Button button) {
        button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                Parent root;
                try{
					if (Campaign.getCampaigns(Authentication.getCurrentUser()).size() == 0) {
						Alert errorAlert = new Alert(Alert.AlertType.ERROR, "You need to have at least one campaign created. Please add one in the 'manage campaigns' menu", ButtonType.OK);
						errorAlert.showAndWait();
						return;
					}
                   FrontendConfig.startEditing(indexFromButton(button));
                    root = FXMLLoader.load(Objects.requireNonNull(getClass().getClassLoader().getResource("Selection_Page.fxml")));
                    Stage chartStage = new Stage();
                    chartStage.setTitle("Metric Selection");
                    Scene scene = new Scene(root, 400, 100);
                    scene.getStylesheets().add(ManageCSS.getFontSizeCSS());
                    chartStage.setScene(scene);
                    chartStage.setResizable(false);
                    chartStage.show();
                    ((Node)(actionEvent.getSource())).getScene().getWindow().hide();
                }
                catch(IOException | SQLException e){
                    e.printStackTrace();
                }
			}
        });
    }

    public void setEditButton(Button button) {
    	button.setOnAction(actionEvent -> {
    		int index = indexFromButton(button);
    		Widget prevWidget = FrontendConfig.getWidget(index);
//    		FrontendConfig.deleteWidget(index);
    		FrontendConfig.startEditing(index);
    		try {
				if (prevWidget instanceof GraphWidget) {
					openChartPage(actionEvent, getClass().getClassLoader());
				} if (prevWidget instanceof ListWidget) {
					openListPage(actionEvent, getClass().getClassLoader());
				} if (prevWidget instanceof HistogramWidget) {
                    openHistogramPage(actionEvent, getClass().getClassLoader());
                }
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}


    private void openChartPage(ActionEvent event, ClassLoader classLoader) throws IOException {
        Parent root;
        root = FXMLLoader.load(getClass().getClassLoader().getResource("Chart_Page.fxml"));
        Stage chartStage = new Stage();
        chartStage.setTitle("Graph Creation");
        Scene scene = new Scene(root, 1300, 800);
        scene.getStylesheets().add(ManageCSS.getFontSizeCSS());
        chartStage.setScene(scene);
        chartStage.setResizable(false);
        chartStage.show();
        ((Node)(event.getSource())).getScene().getWindow().hide();
    }

    private void openListPage(ActionEvent event, ClassLoader classLoader) throws IOException {
        Parent root;
        root = FXMLLoader.load(getClass().getClassLoader().getResource("List_Page.fxml"));
        Stage listStage = new Stage();
        listStage.setTitle("List Creation");
        Scene scene = new Scene(root, 1300, 800);
        scene.getStylesheets().add(ManageCSS.getFontSizeCSS());
        listStage.setScene(scene);
        listStage.setResizable(false);
        listStage.show();
        ((Node)(event.getSource())).getScene().getWindow().hide();
    }

    private void openHistogramPage(ActionEvent event, ClassLoader classLoader) throws IOException {
        Parent root;
        root = FXMLLoader.load(getClass().getClassLoader().getResource("Histogram_Page.fxml"));
        Stage histogramStage = new Stage();
        histogramStage.setTitle("Histogram Creation");
        Scene scene = new Scene(root, 1300, 800);
        scene.getStylesheets().add(ManageCSS.getFontSizeCSS());
        histogramStage.setScene(scene);
        histogramStage.setResizable(false);
        histogramStage.show();
        ((Node)(event.getSource())).getScene().getWindow().hide();
    }

    public void setDeleteButton(Button button) {
    	button.setOnAction(actionEvent -> {
    		int index = indexFromButton(button);
    		FrontendConfig.deleteWidget(index);
    		setWidget(index, null);
		});
	}

    public static Alert createAlertWithOptOut(Alert.AlertType type, String title, String headerText,
                                              String message, String confirmMessage, Consumer<Boolean> optOutAction,
                                              ButtonType... buttonTypes) {
        Alert alert = new Alert(type);
        // Need to force the alert to layout in order to grab the graphic,
        // as we are replacing the dialog pane with a custom pane
        alert.getDialogPane().applyCss();
        Node graphic = alert.getDialogPane().getGraphic();
        // Create a new dialog pane that has a checkbox instead of the hide/show details button
        // Use the supplied callback for the action of the checkbox
        alert.setDialogPane(new DialogPane() {
            @Override
            protected Node createDetailsButton() {
                CheckBox optOut = new CheckBox();
                optOut.setText(confirmMessage);
                optOut.setOnAction(e -> optOutAction.accept(optOut.isSelected()));
                return optOut;
            }
        });
        alert.getDialogPane().getButtonTypes().addAll(buttonTypes);
        alert.getDialogPane().setContentText(message);
        // Fool the dialog into thinking there is some expandable content
        // a Group won't take up any space if it has no children
        alert.getDialogPane().setExpandableContent(new Group());
        alert.getDialogPane().setExpanded(true);
        // Reset the dialog graphic using the default style
        alert.getDialogPane().setGraphic(graphic);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        return alert;
    }

    public void printGraph(ActionEvent actionEvent) {
        Printer printer = Printer.getDefaultPrinter();
        Parent chart = selectedChart(((MenuItem)actionEvent.getSource()).getText());
        if(chart!=null){
            WritableImage chartImage = chart.snapshot(new SnapshotParameters(), null);
            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(chartImage, null);
            if(printer != null){
                PageLayout pageLayout = printer.createPageLayout(Paper.A4, PageOrientation.REVERSE_PORTRAIT, Printer.MarginType.DEFAULT);
                double scaleX = pageLayout.getPrintableWidth() / chart.getBoundsInParent().getWidth();
                chart.getTransforms().add(new Scale(scaleX, 1));

                PrinterJob job = PrinterJob.createPrinterJob();
                if (job != null && job.showPrintDialog(chart.getScene().getWindow())){
                    boolean success = job.printPage(pageLayout, chart);
                    if (success) {
                        job.endJob();
                    }
                }
                chart.getTransforms().clear();
            }
            else
            {
                Alert printerErrorAlert = new Alert(Alert.AlertType.ERROR, "Please check if your printer is connected", ButtonType.OK);
                printerErrorAlert.setTitle("Printer error");
                printerErrorAlert.showAndWait();
            }
        }else{
            Alert printerErrorAlert = new Alert(Alert.AlertType.ERROR, "Please select a valid graph", ButtonType.OK);
            printerErrorAlert.showAndWait();
        }
    }

    public Parent selectedChart(String text){
        Parent graph = null;
        switch (text){
            case "Graph1":
                if(FrontendConfig.getWidget(1)!=null) {
                    graph = (FrontendConfig.getWidget(1)).create();
                    borderPane1.setCenter(graph);
                    return graph;
                }
                break;
            case "Graph2":
                if(FrontendConfig.getWidget(2)!=null) {
                    graph = (FrontendConfig.getWidget(2)).create();
                    borderPane2.setCenter(graph);
                    return graph;
                }
                break;
            case "Graph3":
                if(FrontendConfig.getWidget(3)!=null) {
                    graph = (FrontendConfig.getWidget(3)).create();
                    borderPane3.setCenter(graph);
                    return graph;
                }
                break;
            case "Graph4":
                if(FrontendConfig.getWidget(4)!=null) {
                    graph = (FrontendConfig.getWidget(4)).create();
                    borderPane4.setCenter(graph);
                    return graph;
                }
                break;
            default:
                return null;
        }
        return graph;
    }

    public void saveGraph(ActionEvent actionEvent) {
        fileChooser.setInitialDirectory(new File(Paths.get(".").toAbsolutePath().normalize().toString()));   //current directory
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("PNG","*.png"));
        Parent chart = selectedChart(((MenuItem)actionEvent.getSource()).getText());
        if(chart != null){
            SnapshotParameters snapshotParameters = new SnapshotParameters();
            WritableImage chartImage = chart.snapshot(snapshotParameters, null);
            File newFile = fileChooser.showSaveDialog(null);
            if(newFile != null){
                try {
                    if(!newFile.getName().endsWith(".png")){
                        newFile = new File(newFile.getPath()+".png");
                    }
                    ImageIO.write(SwingFXUtils.fromFXImage(chartImage, null), "png", newFile);
                    Desktop.getDesktop().open(newFile);
                } catch (IOException e) {
                    System.err.println("Failed to save image.");
                }
            }
        }else{
            Alert saveErrorAlert = new Alert(Alert.AlertType.ERROR, "Please select a valid graph", ButtonType.OK);
            saveErrorAlert.showAndWait();
        }
    }

    public List<WritableImage> chartsToImages(){
        List<WritableImage> images = new ArrayList<>();
        SnapshotParameters snapshotParameters = new SnapshotParameters();
        snapshotParameters.setTransform(new Scale(2, 2));
        if(FrontendConfig.getWidget(1)!=null) images.add(selectedChart("Graph1").snapshot(snapshotParameters, null));
        if(FrontendConfig.getWidget(2)!=null) images.add(selectedChart("Graph2").snapshot(snapshotParameters, null));
        if(FrontendConfig.getWidget(3)!=null) images.add(selectedChart("Graph3").snapshot(snapshotParameters, null));
        if(FrontendConfig.getWidget(4)!=null) images.add(selectedChart("Graph4").snapshot(snapshotParameters, null));
        return  images;
    }

    public void saveAllGraph(ActionEvent actionEvent) {
        File file = new File(Paths.get(".").toAbsolutePath().normalize().toString());   //saved to current directory
        List<WritableImage> chartImages = chartsToImages();
        int n = 1;
        if(file != null){
            try {
                for (WritableImage chartImage : chartImages) {
                    File newFile = new File(file.getPath()+"\\chart"+n+".png");
                    ImageIO.write(SwingFXUtils.fromFXImage(chartImage, null), "png", newFile);
                    Desktop.getDesktop().open(newFile);
                    n++;
                }
            } catch (IOException e) {
                System.err.println("Failed to save images.");
            }
        }
    }
}