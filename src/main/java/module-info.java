module com.example.client {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.apache.httpcomponents.httpclient;
    requires org.apache.httpcomponents.httpcore;
    requires com.fasterxml.jackson.databind;
    requires com.google.gson;


    opens com.example.client to javafx.fxml, com.google.gson;
    exports com.example.client;
}