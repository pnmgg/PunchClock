package com.example.punchclock.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "login_logs")
public class LoginLog {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public long userId; // -1 if unknown user
    public String username; // The username attempted
    public long attemptTime;
    public boolean isSuccess;
    public String deviceInfo; // IP or Device model

    @androidx.room.Ignore
    public LoginLog(long userId, String username, long attemptTime, boolean isSuccess, String deviceInfo) {
        this.userId = userId;
        this.username = username;
        this.attemptTime = attemptTime;
        this.isSuccess = isSuccess;
        this.deviceInfo = deviceInfo;
    }

    public LoginLog() {}
}
