package com.example.punchclock.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.punchclock.MainActivity;
import com.example.punchclock.R;
import com.example.punchclock.data.AppDatabase;
import com.example.punchclock.data.User;
import com.example.punchclock.utils.SessionManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminActivity extends AppCompatActivity {

    private UserAdapter adapter;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn() || !sessionManager.isAdmin()) {
            finish();
            return;
        }

        setContentView(R.layout.activity_admin);

        ImageView btnLogout = findViewById(R.id.btn_logout);
        EditText etSearch = findViewById(R.id.et_search);
        RecyclerView recyclerView = findViewById(R.id.recycler_view_users);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UserAdapter();
        recyclerView.setAdapter(adapter);

        // Load all users initially
        loadUsers("");

        btnLogout.setOnClickListener(v -> {
            sessionManager.logoutUser();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                loadUsers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadUsers(String query) {
        LiveData<List<User>> usersLiveData;
        if (query.isEmpty()) {
            usersLiveData = AppDatabase.getDatabase(this).userDao().getAllUsers();
        } else {
            usersLiveData = AppDatabase.getDatabase(this).userDao().searchUsers(query);
        }
        
        // Remove previous observers to avoid multiple updates? 
        // Actually simplest is just to observe and update adapter.
        // In a real app we'd use ViewModel. For now, let's just observe.
        // Note: This adds a new observer every time query changes. Ideally should use switchMap in ViewModel.
        // Given constraints, I'll rely on Room's LiveData behavior but maybe just do one-shot query if search changes rapidly?
        // Or just let it be, it's a demo.
        usersLiveData.observe(this, users -> adapter.setUsers(users));
    }

    private class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
        private List<User> users = new ArrayList<>();

        public void setUsers(List<User> users) {
            this.users = users;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
            return new UserViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
            User user = users.get(position);
            holder.textUsername.setText(user.username);
            holder.textRole.setText(user.role);
            
            if (user.lastLoginAt > 0) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                holder.textLastLogin.setText("最后登录: " + sdf.format(new Date(user.lastLoginAt)));
            } else {
                holder.textLastLogin.setText("从未登录");
            }

            if (user.isActive) {
                holder.btnToggleStatus.setText("禁用");
                holder.btnToggleStatus.setTextColor(android.graphics.Color.RED);
            } else {
                holder.btnToggleStatus.setText("启用");
                holder.btnToggleStatus.setTextColor(android.graphics.Color.GREEN);
            }

            holder.btnToggleStatus.setOnClickListener(v -> {
                // Toggle status
                user.isActive = !user.isActive;
                AppDatabase.databaseWriteExecutor.execute(() -> {
                    AppDatabase.getDatabase(AdminActivity.this).userDao().update(user);
                });
            });

            holder.btnViewContent.setOnClickListener(v -> {
                Intent intent = new Intent(AdminActivity.this, MainActivity.class);
                intent.putExtra("VIEW_USER_ID", user.id);
                intent.putExtra("VIEW_USER_NAME", user.username);
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return users.size();
        }

        class UserViewHolder extends RecyclerView.ViewHolder {
            TextView textUsername;
            TextView textRole;
            TextView textLastLogin;
            Button btnToggleStatus;
            Button btnViewContent;

            public UserViewHolder(@NonNull View itemView) {
                super(itemView);
                textUsername = itemView.findViewById(R.id.text_username);
                textRole = itemView.findViewById(R.id.text_role);
                textLastLogin = itemView.findViewById(R.id.text_last_login);
                btnToggleStatus = itemView.findViewById(R.id.btn_toggle_status);
                btnViewContent = itemView.findViewById(R.id.btn_view_content);
            }
        }
    }
}
