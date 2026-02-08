package com.example.punchclock.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ProjectDao {
    @Query("SELECT * FROM projects ORDER BY createdAt DESC")
    LiveData<List<Project>> getAllProjects();

    @Query("SELECT * FROM projects ORDER BY createdAt DESC")
    List<Project> getAllProjectsSync();

    @Query("SELECT * FROM projects WHERE userId = :userId ORDER BY createdAt DESC")
    LiveData<List<Project>> getProjectsForUser(long userId);

    @Query("SELECT * FROM projects WHERE id = :id")
    Project getProjectById(long id);

    @Insert
    long insert(Project project);

    @Update
    void update(Project project);

    @Delete
    void delete(Project project);
}
