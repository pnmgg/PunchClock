package com.example.punchclock;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.punchclock.data.Project;
import com.example.punchclock.ui.LoginActivity;
import com.example.punchclock.ui.ProjectAdapter;
import com.example.punchclock.ui.detail.ProjectDetailActivity;
import com.example.punchclock.utils.SessionManager;
import com.example.punchclock.viewmodel.PunchViewModel;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private PunchViewModel punchViewModel;
    private SessionManager sessionManager;
    private long targetUserId;
    private boolean isViewingOtherUser = false;

    private static final int CREATE_BACKUP_FILE_REQUEST = 101;
    private static final int OPEN_BACKUP_FILE_REQUEST = 102;

    @Override
    protected void onResume() {
        super.onResume();
        checkSession();
    }

    private void checkSession() {
        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(this, "会话已过期，请重新登录", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        sessionManager = new SessionManager(this);
        // Initial check is handled by onResume or explicit check here
        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Determine target user
        long currentUserId = sessionManager.getUserId();
        long viewUserId = getIntent().getLongExtra("VIEW_USER_ID", -1);
        String viewUserName = getIntent().getStringExtra("VIEW_USER_NAME");

        if (viewUserId != -1 && sessionManager.isAdmin()) {
            targetUserId = viewUserId;
            isViewingOtherUser = true;
            setTitle("管理: " + viewUserName);
        } else {
            targetUserId = currentUserId;
            setTitle("PunchClock");
        }

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        final ProjectAdapter adapter = new ProjectAdapter();
        recyclerView.setAdapter(adapter);

        punchViewModel = new ViewModelProvider(this).get(PunchViewModel.class);
        
        // Observe projects for target user
        punchViewModel.getProjectsForUser(targetUserId).observe(this, projects -> {
            adapter.setProjects(projects);
        });

        adapter.setOnItemClickListener(new ProjectAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Project project) {
                Intent intent = new Intent(MainActivity.this, ProjectDetailActivity.class);
                intent.putExtra(ProjectDetailActivity.EXTRA_PROJECT_ID, project.id);
                intent.putExtra(ProjectDetailActivity.EXTRA_PROJECT_NAME, project.name);
                startActivity(intent);
            }

            @Override
            public void onDeleteClick(Project project) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("删除项目")
                        .setMessage("确定要删除项目 \"" + project.name + "\" 吗？所有打卡记录也将被删除。")
                        .setPositiveButton("删除", (dialog, which) -> punchViewModel.deleteProject(project))
                        .setNegativeButton("取消", null)
                        .show();
            }
        });

        ExtendedFloatingActionButton fab = findViewById(R.id.fab_add_project);
        fab.setOnClickListener(v -> showAddProjectDialog());
    }

    private void showAddProjectDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(isViewingOtherUser ? "为该用户新建项目" : "新建打卡项目");

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_project, null);
        final EditText nameEditText = view.findViewById(R.id.edit_text_name);
        final EditText categoryEditText = view.findViewById(R.id.edit_text_category);
        final EditText descEditText = view.findViewById(R.id.edit_text_description);
        final View advancedLayout = view.findViewById(R.id.layout_advanced_options);
        final View advancedToggle = view.findViewById(R.id.tv_advanced_options);
        
        advancedToggle.setOnClickListener(v -> {
            if (advancedLayout.getVisibility() == View.VISIBLE) {
                advancedLayout.setVisibility(View.GONE);
                ((android.widget.TextView)v).setText("▼ 高级选项");
            } else {
                advancedLayout.setVisibility(View.VISIBLE);
                ((android.widget.TextView)v).setText("▲ 收起选项");
            }
        });

        builder.setView(view);

        builder.setPositiveButton("创建", (dialog, which) -> {
            String name = nameEditText.getText().toString().trim();
            String category = categoryEditText.getText().toString().trim();
            String desc = descEditText.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(MainActivity.this, "请输入项目名称", Toast.LENGTH_SHORT).show();
                return;
            }
            if (category.isEmpty()) {
                Toast.makeText(MainActivity.this, "请输入项目类别", Toast.LENGTH_SHORT).show();
                return;
            }

            // Random color for now
            Random rnd = new Random(); 
            int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));

            Project project = new Project(name, desc, category, color, System.currentTimeMillis(), targetUserId);
            punchViewModel.insertProject(project);
        });

        builder.setNegativeButton("取消", null);

        builder.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            showLogoutConfirmation();
            return true;
        } else if (id == R.id.action_backup) {
            createBackupFile();
            return true;
        } else if (id == R.id.action_restore) {
            openBackupFile();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showLogoutConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("退出登录")
                .setMessage("确定要退出当前账号吗？")
                .setPositiveButton("退出", (dialog, which) -> performLogout())
                .setNegativeButton("取消", null)
                .show();
    }

    private void performLogout() {
        // Show loading if network op involved (not needed for local DB but good practice)
        // Toast.makeText(this, "正在退出...", Toast.LENGTH_SHORT).show();
        
        sessionManager.logoutUser();
        
        Intent intent = new Intent(this, LoginActivity.class);
        // Clear the back stack so user can't go back to Main
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void createBackupFile() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        intent.putExtra(Intent.EXTRA_TITLE, "punch_clock_backup.json");
        // startActivityForResult(intent, CREATE_BACKUP_FILE_REQUEST); 
        // Note: Deprecated but keeping for existing logic compatibility if any
    }

    private void openBackupFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        // startActivityForResult(intent, OPEN_BACKUP_FILE_REQUEST);
    }
}
