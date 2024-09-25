package com.example.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class UserController implements Initializable {
    @FXML
    private TextField urlField;
    @FXML
    private Label email;
    public static Label lblEmail;
    @FXML
    private TextArea htmlContentArea;
    @FXML
    private ListView<String> urlListView;

    private List<String> urls = new ArrayList<>();

    private int uid;

    public RoleSelectionController rl;
    public void updateDisplay(String newValue) {
        email.setText(newValue);
    }

    private ArrayList<User> readUsersFromJson() {
        Gson gson = new Gson();
        Type userListType = new TypeToken<ArrayList<User>>() {}.getType();
        try (FileReader reader = new FileReader("users.json")) {
            return gson.fromJson(reader, userListType);
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>(); // Trả về danh sách rỗng nếu có lỗi
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

    public void setUid(int uid) {
        System.out.println(uid);
        this.uid = uid;
    }
    public int getUid() {
        return uid;
    }
    public void setRoleSelectionController(RoleSelectionController rl) {
        this.rl = rl;
    }

    @FXML
    private void fetchHtml() {
        String url = urlField.getText();
        if (!url.isEmpty()) {
            Task<String> task = new Task<>() {
                @Override
                protected String call() throws Exception {
                    return fetchUrlContent(url);
                }

                @Override
                protected void succeeded() {
                    htmlContentArea.setText(getValue());
                    saveUrlToServer(url);
                    fetchUrlsFromServer();
                    urlField.clear();
                    updateUrlList();
                }

                @Override
                protected void failed() {
                    htmlContentArea.setText("Error fetching the URL");
                }
            };
            new Thread(task).start();
            urlField.clear();
        }
    }

    private String fetchUrlContent(String urlString) throws IOException {
        String apiUrl = "http://localhost:8080/fetch?url=" + urlString; // Địa chỉ server
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(apiUrl);
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                return EntityUtils.toString(response.getEntity());
            }
        }
    }

    public void fetchUrlsFromServer() {
        String apiUrl = "http://localhost:8080/api/urls"; // Địa chỉ API

        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpGet request = new HttpGet(apiUrl);

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                String jsonResponse = EntityUtils.toString(response.getEntity());

                ObjectMapper objectMapper = new ObjectMapper();
                List<String> fetchedUrls = objectMapper.readValue(jsonResponse, new TypeReference<List<String>>() {});

                Platform.runLater(() -> {
                    urlListView.setItems(FXCollections.observableArrayList(fetchedUrls));
                    urls.addAll(fetchedUrls);
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveUrlToServer(String url) {
        String apiUrl = "http://localhost:8080/api/urls"; // Endpoint để lưu URL
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpPost postRequest = new HttpPost(apiUrl);

            // Tạo JSON payload
            ObjectMapper objectMapper = new ObjectMapper();
            UrlRequest urlRequest = new UrlRequest(url);
            String jsonPayload = objectMapper.writeValueAsString(urlRequest);  // Chuyển đổi thành chuỗi JSON

            StringEntity entity = new StringEntity(jsonPayload);
            postRequest.setEntity(entity);
            postRequest.setHeader("Content-Type", "application/json");

            try (CloseableHttpResponse response = httpClient.execute(postRequest)) {
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateUrlList() {
        urlListView.getItems().clear();
        urlListView.getItems().addAll(urls);
    }


    @FXML
    public void start(Stage prst) {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() { return null; }
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                    }
            };

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            e.printStackTrace();
        }

        fetchUrlsFromServer();
        prst.setOnCloseRequest(event -> {
            System.out.println(":adsfas");
            ArrayList<User> list = readUsersFromJson();
            for(User user : list) {
                System.out.println("x"+user.getEmail());
                System.out.println("uid"+lblEmail.getText());
                if(user.getEmail().equals(lblEmail.getText())){
                    user.setActive(false);
                    System.out.println("userss"+user.toString());
                }
            }
            for(User user: list){
                System.out.println("active"+user.isActive());
            }
            writeUsersToJson(list);
        });
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        lblEmail = email;
    }
    public void updateEmail(String emailAddress) {
        lblEmail.setText(emailAddress);
        email.setText(emailAddress);
    }
}
