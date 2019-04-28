package com.comp512.ballBeam.bean;


import com.comp512.ballBeam.game.GameRoom;

import javax.persistence.*;

@Entity
@Table(name = "t_user")
public class User {
    @Id
    @GeneratedValue
    private Long id;

    private String username;

    private String password;

    private float highestPoint;

    private String role = "USER";


    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public User(){

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public float getHighestPoint() {
        return highestPoint;
    }

    public void setHighestPoint(float highestPoint) {
        this.highestPoint = highestPoint;
    }

    public String  getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
