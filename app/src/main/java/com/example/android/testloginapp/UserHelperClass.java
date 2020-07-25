package com.example.android.testloginapp;

public class UserHelperClass {
    String name,userName,email,mobileNumber;

    // Constructor
    public UserHelperClass(String name, String email, String mobileNumber,String userName) {
        this.name = name;
        this.email = email; // contains decoded email except when set from Register_activity
        this.mobileNumber = mobileNumber;
        this.userName = userName;
    }

    // Getters and Setters for class variables



    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }


    public UserHelperClass(){}  // Empty constructor


}
