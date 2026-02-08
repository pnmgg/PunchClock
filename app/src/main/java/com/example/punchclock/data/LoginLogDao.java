package com.example.punchclock.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface LoginLogDao {
    @Insert
    long insert(LoginLog log);

    @Query("SELECT * FROM login_logs ORDER BY attemptTime DESC")
    LiveData<List<LoginLog>> getAllLogs();

    @Query("SELECT * FROM login_logs WHERE userId = :userId ORDER BY attemptTime DESC")
    LiveData<List<LoginLog>> getLogsForUser(long userId);
}
