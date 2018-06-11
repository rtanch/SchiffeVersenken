package de.hhn.aib3.aufg3.gruppe11.authentication;

import java.io.Serializable;

/**
 * Client
 * Holds client specific attributes like id, name and password
 */
public class Client implements Serializable {

    private String id;
    private String username;
    private String password;

    public Client(String username, String password) {
        super();
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
