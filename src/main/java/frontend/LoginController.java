package frontend;

import backend.Authentication;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML
    private Label username_label;
    @FXML
    private TextField username_text;
    @FXML
    private Label pass_label;
    @FXML
    private PasswordField pass_text;
    @FXML
    private Button loginButton;

    public void initialize(URL url, ResourceBundle resourceBundle) {
        loginButton.setDefaultButton(true);
        loginButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Parent root;
                try {
                    if (Authentication.login(username_text.getText(), pass_text.getText())) {
                        try {
							FrontendConfig.reset();
                            root = FXMLLoader.load(getClass().getClassLoader().getResource("Home_Page.fxml"));
                            Stage homeStage = new Stage();
                            homeStage.setTitle("Advertisement System");
                            Scene scene = new Scene(root, 1300, 800);
                            scene.getStylesheets().add(ManageCSS.getFontSizeCSS());
                            homeStage.setScene(scene);
                            homeStage.setResizable(false);
                            homeStage.show();
                            ((Node)(event.getSource())).getScene().getWindow().hide();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    else{
                        Alert errorAlert = new Alert(Alert.AlertType.ERROR, "ERROR: Name or Password is incorrect.", ButtonType.OK);
                        errorAlert.showAndWait();
                    }
                } catch (Authentication.AuthenticationException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
