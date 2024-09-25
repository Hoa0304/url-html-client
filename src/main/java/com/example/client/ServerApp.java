//package com.example.client;
//
//import javafx.application.Application;
//import javafx.fxml.FXMLLoader;
//import javafx.scene.Parent;
//import javafx.scene.Scene;
//import javafx.scene.control.*;
//import javafx.scene.layout.VBox;
//import javafx.stage.Stage;
//import javafx.stage.StageStyle;
//
//import java.io.IOException;
//import java.util.List;
//
//public class ServerApp extends Application {
//
//    private ListView<String> clientListView;
//    private TextArea urlListArea;
//
//    public static void main(String[] args) {
//        launch(args);
//    }
//
//    @Override
//    public void start(Stage primaryStage) {
//        primaryStage.setTitle("Server Dashboard");
//
//        clientListView = new ListView<>();
//        urlListArea = new TextArea();
//        urlListArea.setEditable(false);
//        urlListArea.setPromptText("URLs fetched by clients will appear here...");
//
//        VBox vbox = new VBox(clientListView, urlListArea);
//        Scene scene = new Scene(vbox, 400, 300);
//        primaryStage.setScene(scene);
//        primaryStage.show();
//
//        startServer();
//    }
//
//    private void startServer() {
//        // Code để khởi động server và cập nhật danh sách client
//        new Thread(() -> {
//            // Giả lập danh sách client và URL
//            while (true) {
//                // Cập nhật danh sách client online
//                // Cập nhật URL đã fetch
//                updateClientList();
//                updateUrlList();
//                try {
//                    Thread.sleep(5000); // Cập nhật mỗi 5 giây
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();
//    }
//
//    private void updateClientList() {
//        // Cập nhật danh sách client online
//        clientListView.getItems().clear();
//        clientListView.getItems().addAll("Client 1", "Client 2");
//    }
//
//    private void updateUrlList() {
//        // Cập nhật danh sách URL đã fetch
//        urlListArea.clear();
//        urlListArea.appendText("http://example.com\n"); // Thay bằng danh sách thực tế
//    }
//}

package com.example.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class ServerApp extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("admin.fxml"));
        Parent root = fxmlLoader.load();

        Scene scene = new Scene(root);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setScene(scene);;

        stage.show();

        AdminController ad = fxmlLoader.getController();
        // Khi một client kết nối
        ad.clientConnected("client@example.com");

// Khi một client ngắt kết nối
        ad.clientDisconnected("client@example.com");

//        ad.startServer();
    }

    public static void main(String[] args) {
        launch();
    }


}