package com.example.punchclock.data;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "punch_records",
        foreignKeys = @ForeignKey(entity = Project.class,
                                  parentColumns = "id",
                                  childColumns = "projectId",
                                  onDelete = ForeignKey.CASCADE),
        indices = {@Index("projectId"), @Index("date")})
public class PunchRecord {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public long projectId;
    public long date; // Store as epoch day or timestamp (midnight)

    @androidx.room.Ignore
    public PunchRecord(long projectId, long date) {
        this.projectId = projectId;
        this.date = date;
    }
    
    // Default constructor for Room
    public PunchRecord() {}
}
