package com.example.punchclock.utils;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import com.example.punchclock.data.AppDatabase;
import com.example.punchclock.data.Project;
import com.example.punchclock.data.PunchRecord;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BackupManager {

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    public static class BackupData {
        public List<Project> projects;
        public List<PunchRecord> records;

        public BackupData(List<Project> projects, List<PunchRecord> records) {
            this.projects = projects;
            this.records = records;
        }
    }

    public interface BackupCallback {
        void onSuccess();
        void onError(String error);
    }

    public static void exportData(Context context, Uri uri, BackupCallback callback) {
        executor.execute(() -> {
            try {
                AppDatabase db = AppDatabase.getDatabase(context);
                List<Project> projects = db.projectDao().getAllProjectsSync();
                List<PunchRecord> records = db.punchRecordDao().getAllRecordsSync();

                BackupData data = new BackupData(projects, records);
                String json = new Gson().toJson(data);

                try (OutputStream os = context.getContentResolver().openOutputStream(uri)) {
                    if (os != null) {
                        os.write(json.getBytes(StandardCharsets.UTF_8));
                        mainHandler.post(callback::onSuccess);
                    } else {
                        mainHandler.post(() -> callback.onError("无法打开文件进行写入"));
                    }
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("导出失败: " + e.getMessage()));
            }
        });
    }

    public static void importData(Context context, Uri uri, BackupCallback callback) {
        executor.execute(() -> {
            try {
                StringBuilder stringBuilder = new StringBuilder();
                try (InputStream is = context.getContentResolver().openInputStream(uri);
                     BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        stringBuilder.append(line);
                    }
                }

                String json = stringBuilder.toString();
                Type type = new TypeToken<BackupData>(){}.getType();
                BackupData data = new Gson().fromJson(json, type);

                if (data == null || data.projects == null) {
                    mainHandler.post(() -> callback.onError("无效的备份文件"));
                    return;
                }

                AppDatabase db = AppDatabase.getDatabase(context);
                db.runInTransaction(() -> {
                    // Clear existing data? Or merge? 
                    // Strategy: Simple restore - keep existing, add new if not conflict (IDs might conflict).
                    // Or safer: Clear all and insert.
                    // Let's go with Clear and Insert for simplicity of "Restore" meaning.
                    // But deleting everything is dangerous.
                    // Let's try to upsert.
                    
                    // Actually, for "Restore", usually it means "Replace current state with backup".
                    // But IDs are auto-generated. If we insert with ID, Room respects it.
                    
                    // Let's wipe and replace to be consistent with backup state.
                    // Warning: User should be warned.
                    
                    // Since I don't have a clearAll method, I'll iterate.
                    // Or simpler: Just insert. IDs in backup will be used.
                    // If conflict, REPLACE strategy is used in insert(PunchRecord), but not Project.
                    // Project insert return long, so it's likely ABORT or IGNORE by default if not specified.
                    // Let's modify ProjectDao to use OnConflictStrategy.REPLACE
                    
                    for (Project p : data.projects) {
                        // We need a method to insert or update
                        Project existing = db.projectDao().getProjectById(p.id);
                        if (existing != null) {
                            db.projectDao().update(p);
                        } else {
                            db.projectDao().insert(p);
                        }
                    }
                    
                    for (PunchRecord r : data.records) {
                        db.punchRecordDao().insert(r);
                    }
                });

                mainHandler.post(callback::onSuccess);

            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("导入失败: " + e.getMessage()));
            }
        });
    }
}
