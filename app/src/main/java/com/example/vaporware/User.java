package com.example.vaporware;

public class User {
        public String name;
        public String email;
        public String dob;
        public String password;
        public String display; // New field for display name
        public String profileImageUrl;

        public User() {
            // Default constructor required for DataSnapshot.getValue(User.class)
        }

    public User(String name, String email, String dob, String password, String display, String profileImageUrl) {
        this.name = name;
        this.email = email;
        this.dob = dob;
        this.password = password;
        this.display = display;
        this.profileImageUrl = profileImageUrl;
    }
    }
