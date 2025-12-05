package com.example.myapplication.javabean;

public class User {
    private String etRegUsername;
    private String etRegPassword;
    public User(){
    }
    public User(String etRegUsername,String etRegPassword){
        this.etRegUsername=etRegUsername;
        this.etRegPassword=etRegPassword;
    }

    public String getEtRegUsername() {
        return etRegUsername;
    }

    public String getEtRegPassword() {
        return etRegPassword;
    }

    public void setEtUsername(String etRegUsername) {
        this.etRegUsername = etRegUsername;
    }

    public void setEtPassword(String etRegPassword) {
        this.etRegPassword = etRegPassword;
    }

    @Override
    public String toString() {
        return "User{" + "etRegPassword='" + etRegPassword + '\'' + ", etRegUsername='" + etRegUsername + '\'' + '}';
    }
}
