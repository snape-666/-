package com.example.myapplication.javabean;

public class User {
    private int id;
    private String etRegUsername;
    private String etRegPassword;
    public User(){
    }
    public User(String etRegUsername,String etRegPassword){
        this.etRegUsername=etRegUsername;
        this.etRegPassword=etRegPassword;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEtRegUsername() {
        return etRegUsername;
    }



    public String getEtRegPassword() {
        return etRegPassword;
    }


    public void setEtRegUsername(String etRegUsername) {
        this.etRegUsername = etRegUsername;
    }

    public void setEtRegPassword(String etRegPassword) {
        this.etRegPassword = etRegPassword;
    }

    @Override
    public String toString() {
        return "User{" + "etRegPassword='" + etRegPassword + '\'' + ", etRegUsername='" + etRegUsername + '\'' + '}';
    }
}
