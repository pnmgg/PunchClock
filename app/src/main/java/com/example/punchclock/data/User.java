package com.example.punchclock.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class User {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String username;
    public String email;
    public String passwordHash; // Stores the hashed password
    public String salt; // Stores the salt used for hashing
    public String role; // "USER" or "ADMIN"
    public long createdAt;
    public long lastLoginAt;
    public boolean isActive;

    @androidx.room.Ignore
    public User(String username, String email, String passwordHash, String salt, String role, long createdAt, boolean isActive) {
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.role = role;
        this.createdAt = createdAt;
        this.isActive = isActive;
        this.lastLoginAt = 0;
    }

    public User() {}
}
