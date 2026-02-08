package com.example.punchclock.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.punchclock.data.AppDatabase;
import com.example.punchclock.data.Project;
import com.example.punchclock.data.ProjectDao;
import com.example.punchclock.data.PunchRecord;
import com.example.punchclock.data.PunchRecordDao;

import java.util.List;

public class PunchViewModel extends AndroidViewModel {
    private ProjectDao projectDao;
    private PunchRecordDao punchRecordDao;
    private LiveData<List<Project>> allProjects;

    public PunchViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        projectDao = db.projectDao();
        punchRecordDao = db.punchRecordDao();
        // Default to all projects (legacy behavior), but UI should switch to getProjectsForUser
        allProjects = projectDao.getAllProjects();
    }

    public LiveData<List<Project>> getAllProjects() {
        return allProjects;
    }

    public LiveData<List<Project>> getProjectsForUser(long userId) {
        return projectDao.getProjectsForUser(userId);
    }

    public void insertProject(Project project) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            projectDao.insert(project);
        });
    }

    public void deleteProject(Project project) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            projectDao.delete(project);
        });
    }

    public void updateProject(Project project) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            projectDao.update(project);
        });
    }

    public LiveData<List<PunchRecord>> getRecordsForProject(long projectId) {
        return punchRecordDao.getRecordsForProject(projectId);
    }

    public void insertRecord(PunchRecord record) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            punchRecordDao.insert(record);
        });
    }
    
    public void deleteRecord(long projectId, long date) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            punchRecordDao.deleteByProjectAndDate(projectId, date);
        });
    }
}
