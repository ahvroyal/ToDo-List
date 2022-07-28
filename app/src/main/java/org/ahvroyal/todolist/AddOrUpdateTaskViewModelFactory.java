package org.ahvroyal.todolist;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import org.ahvroyal.todolist.database.AppDatabase;

public class AddOrUpdateTaskViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final AppDatabase database;
    private final int taskId;

    public AddOrUpdateTaskViewModelFactory(AppDatabase database, int taskId) {
        this.database = database;
        this.taskId = taskId;
    }

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        //noinspection unchecked
        return (T) new AddOrUpdateTaskViewModel(database, taskId);
    }

}
