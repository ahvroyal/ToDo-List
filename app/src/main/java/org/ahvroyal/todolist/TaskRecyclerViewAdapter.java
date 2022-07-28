package org.ahvroyal.todolist;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.ahvroyal.todolist.database.Task;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class TaskRecyclerViewAdapter extends RecyclerView.Adapter<TaskRecyclerViewAdapter.TaskViewHolder> {

    private List<Task> tasks;
    private Context context;

    final private ItemClickListener itemClickListener;

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyy", Locale.getDefault());

    public TaskRecyclerViewAdapter(Context context, ItemClickListener itemClickListener) {
        this.context = context;
        this.itemClickListener = itemClickListener;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.task_item, parent, false);

        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = tasks.get(position);

        String description = task.getDescription();
        int priority = task.getPriority();
        Log.i("time saved in db is : ", String.valueOf(task.getUpdatedAt()));
        String updatedAt = simpleDateFormat.format(task.getUpdatedAt());
        Log.i("formatted time is : ", updatedAt);

        holder.taskDescriptionView.setText(description);
        holder.priorityView.setText(String.valueOf(priority));
        holder.updatedAtView.setText(updatedAt);

        GradientDrawable gradientDrawable = (GradientDrawable) holder.priorityView.getBackground();
        int colorId = getPriorityColor(priority);
        gradientDrawable.setColor(colorId);
        Log.i("Color", "color id is : " + colorId);

    }

    private int getPriorityColor(int priority) {
        switch (priority) {
            case 1:
                return ContextCompat.getColor(context, android.R.color.holo_red_dark);
            case 2:
                return ContextCompat.getColor(context, android.R.color.holo_orange_dark);
            case 3:
                return ContextCompat.getColor(context, android.R.color.holo_orange_light);
            default:
                break;
        }
        return 0;
    }

    @Override
    public int getItemCount() {
        if (tasks == null)
            return 0;

        return tasks.size();
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
        notifyDataSetChanged();
    }

    public interface ItemClickListener {
        void onItemClickListener(int itemId);
    }

    class TaskViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView taskDescriptionView;
        TextView updatedAtView;
        TextView priorityView;

        public TaskViewHolder(View itemView) {
            super(itemView);

            taskDescriptionView = itemView.findViewById(R.id.taskDescription);
            updatedAtView = itemView.findViewById(R.id.taskUpdatedAt);
            priorityView = itemView.findViewById(R.id.priorityTextView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int itemId = tasks.get(getAdapterPosition()).getId();
            itemClickListener.onItemClickListener(itemId);
        }
    }
}
