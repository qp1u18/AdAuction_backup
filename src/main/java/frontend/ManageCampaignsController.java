package frontend;

import backend.Authentication;
import backend.Campaign;
import backend.Client;
import backend.Database;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class ManageCampaignsController implements Initializable {
	@FXML
	private Button close;
	@FXML
	private TextField newCampaignName;
	@FXML
	private Button addCampaign;
	@FXML
	private ComboBox<String> selectClient;
	@FXML
	private ListView<String> currentCampaigns;
	@FXML
	private Button deleteCampaign;

	@FXML
	private TextField newClientName;
	@FXML
	private TextField newClientPassword;
	@FXML
	private Button addClient;
	@FXML
	private ListView<String> currentClients;
	@FXML
	private Button deleteClient;
	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {

		addCampaign.setTooltip(new Tooltip("Add a campaign to the system."));
		deleteCampaign.setTooltip(new Tooltip("Delete the selected campaign."));

		addClient.setTooltip(new Tooltip("Add a client to the system."));
		deleteClient.setTooltip(new Tooltip("Delete the selected client."));

		close.setOnAction(event -> {
			Stage stage = (Stage) close.getScene().getWindow();
			stage.close();
		});

		setupCampaigns();

		if (Authentication.getCurrentUser() == 0) {
			setupClients();
		}
		else {
			hideNode(newClientName);
			hideNode(newClientPassword);
			hideNode(addClient);
			hideNode(currentClients);
			hideNode(deleteClient);
		}


	}
	
	private void setupCampaigns() {
		currentCampaigns.setItems(Campaign.campaignNameList(Authentication.getCurrentUser()));

		if (Authentication.getCurrentUser() == 0) {
			showNode(selectClient);
			ObservableList<String> strings = Client.clientNameList();
			selectClient.setItems(strings);
			selectClient.setValue(strings.get(0));
		}
		else {
			hideNode(selectClient);
		}

		hideNode(deleteCampaign);

		currentCampaigns.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			showNode(deleteCampaign);
			deleteCampaign.setOnAction(actionEvent -> {
				try {
					Campaign.getFromName(newValue).delete();
					currentCampaigns.getItems().remove(newValue);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			});
		});



		addCampaign.setOnAction(event -> {
			String name = newCampaignName.getText();
			if (name == null || name.equals("") || name.equals(" ")) {
				Alert alert = new Alert(Alert.AlertType.ERROR);
				alert.setTitle("Invalid name");
				alert.setHeaderText("Please enter a name for the new campaign");

				alert.showAndWait();
				return;
			}
			try {
				int userId = Authentication.getCurrentUser();
				if (userId == 0) {
					userId = Client.getFromName(selectClient.getValue()).getId();
				}
				Database.insertCampaign(name, userId);
				Database.commitData();

				newCampaignName.clear();
				currentCampaigns.getItems().add(name);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		});
	}

	private void setupClients() {
		currentClients.setItems(Client.clientNameList());

		hideNode(deleteClient);


		currentClients.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			showNode(deleteClient);
			deleteClient.setOnAction(actionEvent -> {
				try {
					Client.getFromName(newValue).delete();
					currentClients.getItems().remove(newValue);
					selectClient.getItems().remove(newValue);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			});
		});



		addClient.setOnAction(event -> {
			String name = newClientName.getText();
			String password = newClientPassword.getText();
			if (name == null || name.equals("") || name.equals(" ")) {
				Alert alert = new Alert(Alert.AlertType.ERROR);
				alert.setTitle("Invalid name");
				alert.setHeaderText("Please enter a name for the new client");

				alert.showAndWait();
				return;
			}
			try {
				Authentication.register(name, password);
				newClientName.clear();
				newClientPassword.clear();
				currentClients.getItems().add(name);
				selectClient.getItems().add(name);
			} catch (Authentication.AuthenticationException e) {
				e.printStackTrace();
			}
		});
	}

	public static void popen(Object parent) {
		try {
			Stage popup = new Stage();

			popup.initModality(Modality.APPLICATION_MODAL);
			popup.setTitle("Settings");

			Parent layout = FXMLLoader.load(parent.getClass().getClassLoader().getResource("Manage_Campaigns.fxml"));
			Scene scene = new Scene(layout, 600, 400);

			popup.setScene(scene);

			popup.showAndWait();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void hideNode(Node node) {
		node.setVisible(false);
		node.setManaged(false);
	}

	private void showNode(Node node) {
		node.setVisible(true);
		node.setManaged(true);
	}
}
