package com.example.punchclock.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface PunchRecordDao {
    @Query("SELECT * FROM punch_records WHERE projectId = :projectId")
    LiveData<List<PunchRecord>> getRecordsForProject(long projectId);

    @Query("SELECT * FROM punch_records WHERE projectId = :projectId AND date >= :startDate AND date <= :endDate")
    LiveData<List<PunchRecord>> getRecordsForProjectInRange(long projectId, long startDate, long endDate);

    @Query("SELECT * FROM punch_records")
    List<PunchRecord> getAllRecordsSync();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(PunchRecord record);

    @Delete
    void delete(PunchRecord record);
    
    @Query("DELETE FROM punch_records WHERE projectId = :projectId AND date = :date")
    void deleteByProjectAndDate(long projectId, long date);
}
