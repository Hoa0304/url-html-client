package com.example.client;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;

public class RoleSelectionController extends UserController{

    @FXML
    private CheckBox userCheckbox;

    @FXML
    private CheckBox adminCheckbox;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private StackPane viewContainer;

    private List<String> selectedRoles = new ArrayList<>();
    private ArrayList<User> listUsers = new ArrayList<>();
    private ArrayList<User> listUserOnline = new ArrayList<>();

    public void setListUsers(ArrayList<User> listUsers) {
        this.listUsers = listUsers;
    }

    public ArrayList<User> getListUserOnline() {
        return listUserOnline;
    }

    public void setListUserOnline(ArrayList<User> listUserOnline) {
        this.listUserOnline = listUserOnline;
    }

    private UserController userController;

    public void setUserController(UserController userController) {
        this.userController = userController;
    }

    @FXML
    private void handleRoleChange() {
        selectedRoles.clear();
        if (userCheckbox.isSelected()) {
            selectedRoles.add("user");
        }
        if (adminCheckbox.isSelected()) {
            selectedRoles.add("admin");
        }
    }

    @FXML
    private void handleSubmit(ActionEvent event) throws IOException {
        String email = emailField.getText();
        String password = passwordField.getText();
        Random rd = new Random();
        int id = rd.nextInt(100);

        if (email.isEmpty() || password.isEmpty() || selectedRoles.isEmpty()) {
            showAlert("Error", "Please fill in all fields and select at least one role.");
            return;
        }

        // Cập nhật email trong UserController
        if (userController != null) {
            userController.updateEmail(email);
        }


        if (selectedRoles.contains("user")) {

            User newUser = new User(id, "", true, email, password);
            listUsers.add(newUser);
            listUserOnline.add(newUser);

            ArrayList<User> existingUsers = readUsersFromJson();
            existingUsers.add(newUser);
            writeUsersToJson(existingUsers);
            switchToScene1(event);
        } else if (selectedRoles.contains("admin")) {
            switchToScene2(event);
        }

        showAlert("Success", "Logged in successfully! Roles: " + selectedRoles);
    }

    public void switchToScene1(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("user.fxml"));
        Parent root = loader.load();

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();

        UserController controller = loader.getController();
        controller.start(stage);
        controller.updateEmail(emailField.getText());
    }

    public void switchToScene2(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("admin.fxml"));
        Parent root = loader.load();
        AdminController controller = loader.getController();
        controller.setHl(listUserOnline);

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private ArrayList<User> readUsersFromJson() {
        Gson gson = new Gson();
        try (FileReader reader = new FileReader("users.json")) {
            Type userListType = new TypeToken<ArrayList<User>>() {}.getType();
            return gson.fromJson(reader, userListType);
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private void writeUsersToJson(ArrayList<User> users) {
        Gson gson = new Gson();
        try (FileWriter writer = new FileWriter("users.json")) {
            gson.toJson(users, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    }
}
