package frontend;

import backend.Settings;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class SettingsController implements Initializable {
	@FXML
	private ComboBox fontSizeSelector;
	@FXML
	private ComboBox fontStyleSelector;
	@FXML
	private ComboBox bounceRateType;
	@FXML
	private TextField bounceRateValue;
	@FXML
	private Button backButton;

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		String[] fontValues = new String[]{"8","9","10","11","12","13","14","15","16","17","18","19","20"};
		fontSizeSelector.setItems(FXCollections.observableArrayList(fontValues));
		fontSizeSelector.setPromptText(ManageCSS.getCurrentFontSize());

		fontSizeSelector.setOnAction(event -> ManageCSS.setFontSize(fontSizeSelector.getSelectionModel().getSelectedItem().toString()));

		String[] fontStyles = new String[]{"Arial","Comic Sans","Courier New","Georgia","Lucida Console","Palatino","Tahoma","Times New Roman"};
		fontStyleSelector.setItems(FXCollections.observableArrayList(fontStyles));
		fontStyleSelector.setPromptText(ManageCSS.getCurrentFontStyle());

		fontStyleSelector.setOnAction(event -> ManageCSS.setFontStyle(fontStyleSelector.getSelectionModel().getSelectedItem().toString()));

		bounceRateType.setItems(FXCollections.observableArrayList(Settings.BounceRateType.labelValues()));
		bounceRateType.setPromptText(Settings.getBouncerateType().label);
		bounceRateType.setOnAction(event -> Settings.setBouncerateType(Settings.BounceRateType.valueOfLabel(bounceRateType.getSelectionModel().getSelectedItem().toString())));

		bounceRateValue.textProperty().setValue(Settings.getBouncerateValue().toString());
		bounceRateValue.textProperty().addListener((observable, oldValue, newValue) -> Settings.setBouncerateValue(newValue));

		backButton.setTooltip(new Tooltip("Return to dashboard."));

		backButton.setOnAction(event -> {
			Stage stage = (Stage) backButton.getScene().getWindow();
			stage.close();
		});
	}
}
