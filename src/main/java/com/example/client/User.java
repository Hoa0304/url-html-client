package com.example.client;

import java.util.ArrayList;

public class User{
    int id;
    String urlRequest;
    boolean active;
    String email;
    String password;
    ArrayList<String> userUrl;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUrlRequest() {
        return urlRequest;
    }

    public void setUrlRequest(String urlRequest) {
        this.urlRequest = urlRequest;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public ArrayList<String> getUserUrl() {
        return userUrl;
    }

    public void setUserUrl(ArrayList<String> userUrl) {
        this.userUrl = userUrl;
    }

    public User(int id, String urlRequest, boolean active, String email, String password) {
        this.id = id;
        this.urlRequest = urlRequest;
        this.active = active;
        this.email = email;
        this.password = password;
        this.userUrl = new ArrayList<>();
    }

    public User() {
    }
}