package com.ledgepro.model;

public class AuthResponse {
    private boolean authenticated;
    private String token;
    private User user;
    private Business business;
    private String message;

    public AuthResponse() {}

    public AuthResponse(boolean authenticated, String token, User user, Business business, String message) {
        this.authenticated = authenticated;
        this.token = token;
        this.user = user;
        this.business = business;
        this.message = message;
    }

    public boolean isAuthenticated() { return authenticated; }
    public void setAuthenticated(boolean authenticated) { this.authenticated = authenticated; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Business getBusiness() { return business; }
    public void setBusiness(Business business) { this.business = business; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
