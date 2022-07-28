package org.ahvroyal.todolist;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import org.ahvroyal.todolist.database.AppDatabase;
import org.ahvroyal.todolist.database.Task;

import java.util.List;

public class MainViewModel extends AndroidViewModel {

    private static final String TAG = MainViewModel.class.getSimpleName();
    private LiveData<List<Task>> tasksLiveData;

    public MainViewModel(Application application) {
        super(application);

        AppDatabase database = AppDatabase.getInstance(this.getApplication());
        tasksLiveData = database.taskDao().loadAllTasks();
        Log.i(TAG, "Live data changed (main) ***");
    }

    public LiveData<List<Task>> getTasksLiveData() {
        Log.i(TAG, "Live data retrieved (main) ***");
        return tasksLiveData;
    }

}
