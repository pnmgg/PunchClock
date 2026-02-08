package com.example.punchclock.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface UserDao {
    @Insert
    long insert(User user);

    @Update
    void update(User user);

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    User getUserByName(String username);

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    User getUserById(long id);

    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    LiveData<List<User>> getAllUsers();

    @Query("SELECT * FROM users WHERE username LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    LiveData<List<User>> searchUsers(String query);
}
