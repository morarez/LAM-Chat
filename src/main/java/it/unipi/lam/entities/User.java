package it.unipi.lam.entities;

import java.util.Objects;

public class User {

    private String username;
    private boolean online;

    public User(){
        this.username = null;
        this.online = false;
    }

    public User(String username){
        this.username = username;
        this.online = true;
    }

    public String getUsername() {
        return username;
    }

    public boolean isOnline() {
        return online;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void changeStatus(boolean online) {
        this.online = online;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return online == user.online && Objects.equals(username, user.username);
    }

}
