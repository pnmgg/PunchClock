package com.example.punchclock.ui;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.punchclock.R;
import com.example.punchclock.data.Project;

import java.util.ArrayList;
import java.util.List;

public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.ProjectViewHolder> {

    private List<Project> projects = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Project project);
        void onDeleteClick(Project project);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setProjects(List<Project> projects) {
        this.projects = projects;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_project, parent, false);
        return new ProjectViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectViewHolder holder, int position) {
        Project currentProject = projects.get(position);
        holder.textViewName.setText(currentProject.name);
        holder.textViewDescription.setText(currentProject.description);
        holder.colorView.setBackgroundTintList(ColorStateList.valueOf(currentProject.color));
    }

    @Override
    public int getItemCount() {
        return projects.size();
    }

    class ProjectViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewName;
        private TextView textViewDescription;
        private View colorView;
        private ImageView deleteButton;

        public ProjectViewHolder(View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.project_name);
            textViewDescription = itemView.findViewById(R.id.project_description);
            colorView = itemView.findViewById(R.id.project_color);
            deleteButton = itemView.findViewById(R.id.delete_button);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (listener != null && position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(projects.get(position));
                    }
                }
            });

            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (listener != null && position != RecyclerView.NO_POSITION) {
                        listener.onDeleteClick(projects.get(position));
                    }
                }
            });
        }
    }
}
