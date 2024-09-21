package com.example.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
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
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

public class HelloController {
    @FXML
    private TextField urlField;
    @FXML
    private TextArea htmlContentArea;
    @FXML
    private ListView<String> urlListView;

    private List<String> urls = new ArrayList<>();

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
                    updateUrlList();  // Cập nhật danh sách URL sau khi đã lưu
                }

                @Override
                protected void failed() {
                    htmlContentArea.setText("Error fetching the URL");
                }
            };
            new Thread(task).start();
            urlField.clear();  // Xóa trường nhập URL
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
                    urls.clear();  // Xóa danh sách URL cũ
                    urls.addAll(fetchedUrls);  // Cập nhật danh sách URL mới
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
            UrlRequest urlRequest = new UrlRequest(url);  // Khởi tạo đối tượng UrlRequest với URL
            String jsonPayload = objectMapper.writeValueAsString(urlRequest);  // Chuyển đổi thành chuỗi JSON

            StringEntity entity = new StringEntity(jsonPayload);
            postRequest.setEntity(entity);
            postRequest.setHeader("Content-Type", "application/json");

            try (CloseableHttpResponse response = httpClient.execute(postRequest)) {
                // Xử lý phản hồi nếu cần
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
    public void start() {
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
    }
}
