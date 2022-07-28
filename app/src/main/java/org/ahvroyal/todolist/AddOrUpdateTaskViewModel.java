package org.ahvroyal.todolist;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import org.ahvroyal.todolist.database.AppDatabase;
import org.ahvroyal.todolist.database.Task;

import java.util.List;

public class AddOrUpdateTaskViewModel extends ViewModel {

    private static final String TAG = AddOrUpdateTaskViewModel.class.getSimpleName();
    private LiveData<Task> taskLiveData;

    public AddOrUpdateTaskViewModel(AppDatabase database, int taskId) {
        taskLiveData = database.taskDao().getTaskById(taskId);
        Log.i(TAG, "Live data changed (update) ***");
    }

    public LiveData<Task> getTaskLiveData() {
        Log.i(TAG, "Live data retrieved (update) ***");
        return taskLiveData;
    }

}
