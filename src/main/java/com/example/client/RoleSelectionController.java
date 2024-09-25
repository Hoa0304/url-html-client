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

import java.io.*;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
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
            initJson();
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
    public void initJson(){
        String filePath = "users.json";
        // Đọc dữ liệu JSON từ file
        StringBuilder jsonData = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                jsonData.append(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        try {
            URL url = new URL("http://<IP_ADDRESS>:8080/api/data");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonData.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                System.out.println("Data sent successfully");
            } else {
                System.out.println("POST request failed: " + conn.getResponseCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        // Nhận dữ liệu JSON từ máy chủ
        try {
            URL url = new URL("http://<IP_ADDRESS>:8080/api/data");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // Ghi đè dữ liệu JSON vào file
                try (FileWriter fileWriter = new FileWriter(filePath, false)) { // false để ghi đè
                    fileWriter.write(response.toString());
                    System.out.println("Received JSON written to file, overwriting old content");
                }
            } else {
                System.out.println("GET request failed: " + conn.getResponseCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
