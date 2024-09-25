package com.example.client;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.util.Duration;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class AdminController {

    @FXML
    private ListView<String> activePeople;
    @FXML
    private ListView<String> allUrlList;

    public ArrayList<User>  hl;

    public ArrayList<User> getHl() {
        return hl;
    }

    public void setHl(ArrayList<User> hl) {
        this.hl = hl;
    }
    private Set<String> connectedClients = new HashSet<>(); // Sử dụng HashSet để lưu trữ client


    @FXML
    public void initialize() {
        loadActivePeople();
        loadAllUrls();
        activePeople.getItems().clear();

        Timeline tl = new Timeline(new KeyFrame(Duration.seconds(5), event -> loadActivePeople()));
        tl.setCycleCount(Timeline.INDEFINITE);
        tl.play();
    }
    private ArrayList<User> readUsersFromJson() {
        Gson gson = new Gson();
        Type userListType = new TypeToken<ArrayList<User>>() {}.getType();
        try (FileReader reader = new FileReader("users.json")) {
            return gson.fromJson(reader, userListType);
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private void loadActivePeople() {
        hl = readUsersFromJson();
        if (hl != null && !hl.isEmpty()) {
            ObservableList<String> people = FXCollections.observableArrayList();
            for (User user : hl) {
                if(user.isActive()==true){
                people.add(user.getEmail());
                connectedClients.add(user.getEmail());
                }
            }
            activePeople.setItems(people);
        } else {
            System.out.println("No active users to load.");
        }
    }

    public void clientConnected(String clientName) {
        connectedClients.add(clientName);
        updateClientList();
    }

    public void clientDisconnected(String clientName) {
        connectedClients.remove(clientName);
        updateClientList();
    }

    private void updateClientList() {
        ObservableList<String> clients = FXCollections.observableArrayList(connectedClients);
        activePeople.setItems(clients);
    }

//    private void updateClientList() {
//        activePeople.getItems().addAll("Client 1", "Client 2");
//    }

//    public void startServer() {
//        new Thread(() -> {
//            while (true) {
//                updateClientList();
//                try {
//                    Thread.sleep(5000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();
//    }


    private void loadAllUrls() {
        // Call the backend API to load URLs
        Service<ObservableList<String>> service = new Service<>() {
            @Override
            protected Task<ObservableList<String>> createTask() {
                return new Task<>() {
                    @Override
                    protected ObservableList<String> call() throws Exception {
                        URL url = new URL("http://10.60.204.129:8080/api/urls");
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("GET");
                        connection.connect();

                        int responseCode = connection.getResponseCode();
                        if (responseCode == 200) {
                            Scanner scanner = new Scanner(url.openStream());
                            ObservableList<String> urls = FXCollections.observableArrayList();
                            while (scanner.hasNextLine()) {
                                urls.add(scanner.nextLine());
                            }
                            scanner.close();
                            return urls;
                        } else {
                            throw new Exception("Failed to fetch URLs");
                        }
                    }
                };
            }
        };
        service.setOnSucceeded(e -> allUrlList.setItems(service.getValue()));
        service.setOnFailed(e -> System.out.println("Failed to load URLs"));
        service.start();
    }
}