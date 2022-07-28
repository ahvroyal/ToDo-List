package org.ahvroyal.todolist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import org.ahvroyal.todolist.database.AppDatabase;
import org.ahvroyal.todolist.database.Task;

import java.util.Date;

public class AddOrUpdateTaskActivity extends AppCompatActivity {

    // Constant for default task id to be used when not in update mode
    private static final int DEFAULT_TASK_ID = -1;

    // Extra for the request code (key)
    public static final String REQ_CODE_KEY = "reqCodeKey";

    // request code
    private int requestCode = 0;

    // Extra for the task ID to be received in updating mode
    public static final String UPDATE_TASK_ID = "updateTaskId";

    // Extra for the task ID to be received after rotation
    public static final String ROTATION_TASK_ID = "rotationTaskId";

    private int taskId = DEFAULT_TASK_ID;

    // Constants for priority
    public static final int PRIORITY_HIGH = 1;
    public static final int PRIORITY_MEDIUM = 2;
    public static final int PRIORITY_LOW = 3;

    // Constant for logging
    private static final String TAG = AddOrUpdateTaskActivity.class.getSimpleName();

    // Fields for views
    EditText mEditText;
    RadioGroup mRadioGroup;
    Button mButton;

    CoordinatorLayout coordinatorLayout;

    private AppDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_or_update_task);

        database = AppDatabase.getInstance(getApplicationContext());

        // initialization of the views
        coordinatorLayout = findViewById(R.id.coordinatorAddOrUpdate);
        mEditText = findViewById(R.id.etTaskDescription);
        mRadioGroup = findViewById(R.id.rgPriority);

        ActionBar actionBar = getSupportActionBar();

        mButton = findViewById(R.id.btnSave);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSaveButtonClicked();
            }
        });

        Intent intent = getIntent();

        if (intent != null && intent.hasExtra(REQ_CODE_KEY)) {
            requestCode = intent.getIntExtra(REQ_CODE_KEY, 0);
            if (requestCode == 1) {
                // to add a new task
                if (actionBar != null) {
                    actionBar.setTitle(R.string.add_task_activity_name);
                }
                mButton.setText(R.string.add_button);

            } else if (requestCode == 2) {
                // update an existing task
                if (actionBar != null) {
                    actionBar.setTitle(R.string.update_task_activity_name);
                }
                mButton.setText(R.string.update_button);

                if (intent.hasExtra(UPDATE_TASK_ID)) {
                    taskId = intent.getIntExtra(UPDATE_TASK_ID, DEFAULT_TASK_ID);

                    // using live data in tandem with view model
                    AddOrUpdateTaskViewModelFactory factory = new AddOrUpdateTaskViewModelFactory(database, taskId);
                    AddOrUpdateTaskViewModel addOrUpdateTaskViewModel = new ViewModelProvider(this, (ViewModelProvider.Factory) factory).get(AddOrUpdateTaskViewModel.class);
                    addOrUpdateTaskViewModel.getTaskLiveData().observe(this, new Observer<Task>() {
                        @Override
                        public void onChanged(Task task) {
                            addOrUpdateTaskViewModel.getTaskLiveData().removeObserver(this);
                            updateUI(task);
                        }
                    });

                    // using live data
//                    LiveData<Task> taskLiveData = database.taskDao().getTaskById(taskId);
//                    taskLiveData.observe(this, new Observer<Task>() {
//                        @Override
//                        public void onChanged(Task task) {
//                            taskLiveData.removeObserver(this);
//                            updateUI(task);
//                        }
//                    });

                    // using executors
//                    AppExecutors.getInstance().diskIO().execute(new Runnable() {
//                        @Override
//                        public void run() {
//                            Task task = database.taskDao().getTaskById(taskId);
//
//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    updateUI(task);
//                                }
//                            });
//                        }
//                    });
                }
            }
        }

        // device rotated ?
        if (savedInstanceState != null && savedInstanceState.containsKey(ROTATION_TASK_ID)) {
            taskId = savedInstanceState.getInt(ROTATION_TASK_ID, DEFAULT_TASK_ID);
        }

    }

    private void updateUI(Task task) {
        if (task == null)
            return;

        mEditText.setText(task.getDescription());
        setPriority(task.getPriority());
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt(ROTATION_TASK_ID, taskId);
        super.onSaveInstanceState(outState);
    }

    private void onSaveButtonClicked() {
        String description = mEditText.getText().toString();
        int priority = getSelectedPriority();
        Date date = new Date();
        Log.i(TAG, "current time is : " + date.toString() + " --- " + date.getTime());

        Task task = new Task(description, priority, date);

        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                Intent intentResult = new Intent();
                int result = 0;
                if (taskId == DEFAULT_TASK_ID) {
                    database.taskDao().insertTask(task);
                    result = 1;
                } else {
                    task.setId(taskId);
                    database.taskDao().updateTask(task);
                    result = 2;
                }
                intentResult.putExtra("result_key", result);
                setResult(RESULT_OK, intentResult);
                finish();
            }
        });

//        Thread thread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                database.taskDao().insertTask(task);
//                finish();
//            }
//        });
//        thread.start();

    }

    private int getSelectedPriority() {
        int checkedId = ((RadioGroup) findViewById(R.id.rgPriority)).getCheckedRadioButtonId();
        switch (checkedId) {
            case R.id.rbHigh:
                return PRIORITY_HIGH;
            case R.id.rbMedium:
                return PRIORITY_MEDIUM;
            case R.id.rbLow:
                return PRIORITY_LOW;
            default:
                break;
        }
        return 1;
    }

    public void setPriority(int priority) {
        switch (priority) {
            case PRIORITY_HIGH:
                ((RadioGroup) findViewById(R.id.rgPriority)).check(R.id.rbHigh);
                break;
            case PRIORITY_MEDIUM:
                ((RadioGroup) findViewById(R.id.rgPriority)).check(R.id.rbMedium);
                break;
            case PRIORITY_LOW:
                ((RadioGroup) findViewById(R.id.rgPriority)).check(R.id.rbLow);
        }
    }

}