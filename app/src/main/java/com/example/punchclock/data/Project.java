package com.example.punchclock.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "projects")
public class Project {
    @PrimaryKey(autoGenerate = true)
    public long id;
    
    public String name;
    public String description;
    public String category; // e.g. "Work", "Study", "Fitness"
    public int color; // Using color as icon representation for simplicity
    public long createdAt;
    public long userId; // Owner of the project

    @androidx.room.Ignore
    public Project(String name, String description, String category, int color, long createdAt, long userId) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.color = color;
        this.createdAt = createdAt;
        this.userId = userId;
    }
    
    // Default constructor for Room
    public Project() {}
}
