package frontend;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class SelectionPageController implements Initializable {
    @FXML
    private Button graphButton;
    @FXML
    private Button listButton;
    @FXML
    private Button histogramButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        graphButton.setTooltip(new Tooltip("Select to display a graph."));
        listButton.setTooltip(new Tooltip("Select to display a list of metrics."));
        histogramButton.setTooltip(new Tooltip("Select to display a histogram"));

        graphButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Parent root;
                try {
                    root = FXMLLoader.load(getClass().getClassLoader().getResource("Chart_Page.fxml"));
                    Stage chartStage = new Stage();
                    chartStage.setTitle("Graph Creation");
                    Scene scene = new Scene(root, 1300, 800);
                    scene.getStylesheets().add(ManageCSS.getFontSizeCSS());
                    chartStage.setScene(scene);
                    chartStage.setResizable(false);
                    chartStage.show();
                    ((Node)(event.getSource())).getScene().getWindow().hide();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        listButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Parent root;
                try {
                    root = FXMLLoader.load(getClass().getClassLoader().getResource("List_Page.fxml"));
                    Stage listStage = new Stage();
                    listStage.setTitle("List Creation");
                    Scene scene = new Scene(root, 1300, 800);
                    scene.getStylesheets().add(ManageCSS.getFontSizeCSS());
                    listStage.setScene(scene);
                    listStage.setResizable(false);
                    listStage.show();
                    ((Node)(event.getSource())).getScene().getWindow().hide();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        histogramButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Parent root;
                try {
                    root = FXMLLoader.load(getClass().getClassLoader().getResource("Histogram_Page.fxml"));
                    Stage histogramStage = new Stage();
                    histogramStage.setTitle("Histogram Creation");
                    Scene scene = new Scene(root, 1300, 800);
                    scene.getStylesheets().add(ManageCSS.getFontSizeCSS());
                    histogramStage.setScene(scene);
                    histogramStage.setResizable(false);
                    histogramStage.show();
                    ((Node)(event.getSource())).getScene().getWindow().hide();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
