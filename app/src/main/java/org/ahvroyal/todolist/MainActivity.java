package org.ahvroyal.todolist;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
//import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import org.ahvroyal.todolist.database.AppDatabase;
import org.ahvroyal.todolist.database.Task;

import java.util.List;

public class MainActivity extends AppCompatActivity implements TaskRecyclerViewAdapter.ItemClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int INTENT_REQ_CODE_ADD = 11;
    private static final int INTENT_REQ_CODE_UPDATE = 12;

    private FloatingActionButton fabButton;
    private RecyclerView recyclerView;
    private TaskRecyclerViewAdapter recyclerViewAdapter;

    private CoordinatorLayout coordinatorLayout;
    private LinearLayout linearLayout;

    private AppDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        database = AppDatabase.getInstance(getApplicationContext());

        coordinatorLayout = findViewById(R.id.coordinatorMain);
        linearLayout = findViewById(R.id.linearLayout);
        fabButton = findViewById(R.id.fabAdd);
        recyclerView = findViewById(R.id.recyclerViewTasks);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        recyclerViewAdapter = new TaskRecyclerViewAdapter(this, this);
        recyclerView.setAdapter(recyclerViewAdapter);

        DividerItemDecoration decoration = new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(decoration);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                AlertDialog.Builder alertDialogDeletion = new AlertDialog.Builder(MainActivity.this);
                alertDialogDeletion.setTitle("Item Delete");
                alertDialogDeletion.setMessage("Are you sure you want to delete this item ?");
                alertDialogDeletion.setCancelable(false);
                alertDialogDeletion.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AppExecutors.getInstance().diskIO().execute(new Runnable() {
                            @Override
                            public void run() {
                                int pos = viewHolder.getAdapterPosition();
                                List<Task> tasks = recyclerViewAdapter.getTasks();
                                Task task = tasks.get(pos);
                                database.taskDao().deleteTask(task);

//                                fetchTasksFromDB();

                            }
                        });
                        MediaPlayer mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.empty_trash_sound_effect);
                        mediaPlayer.start();

                        Snackbar.make(coordinatorLayout, "Item deleted successfully", BaseTransientBottomBar.LENGTH_SHORT).show();
                    }
                });
                alertDialogDeletion.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        fetchTaskOrShow();
                    }
                });
                alertDialogDeletion.setIcon(android.R.drawable.ic_dialog_alert);
                AlertDialog dialog = alertDialogDeletion.create();
                dialog.show();

            }
        }).attachToRecyclerView(recyclerView);

        fabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create a new intent to start an AddTaskActivity
                Intent addTaskIntent = new Intent(MainActivity.this, AddOrUpdateTaskActivity.class);
                addTaskIntent.putExtra(AddOrUpdateTaskActivity.REQ_CODE_KEY, 1);
                startActivityForResult(addTaskIntent, INTENT_REQ_CODE_ADD);
//                startActivity(addTaskIntent);
            }
        });

//        fetchTasksFromDB();

        fetchTaskOrShow();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == INTENT_REQ_CODE_ADD) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    int result = data.getIntExtra("result_key", 0);
                    if (result == 1)
                        Snackbar.make(coordinatorLayout, "Item inserted successfully", BaseTransientBottomBar.LENGTH_SHORT).show();
                }
            }

        } else if (requestCode == INTENT_REQ_CODE_UPDATE) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    int result = data.getIntExtra("result_key", 0);
                    if (result == 2)
                        Snackbar.make(coordinatorLayout, "Item updated successfully", BaseTransientBottomBar.LENGTH_SHORT).show();
                }
            }

        }
    }

    // using view model to cache data
    private void fetchTaskOrShow() {
//        MainViewModel viewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        MainViewModel mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        mainViewModel.getTasksLiveData().observe(this, new Observer<List<Task>>() {
            @Override
            public void onChanged(List<Task> tasks) {
                Log.i(TAG, "Querying DB ***");
                recyclerViewAdapter.setTasks(tasks);

                if (tasks.size() <= 0) {
                    recyclerView.setVisibility(View.INVISIBLE);
                    linearLayout.setVisibility(View.VISIBLE);
                } else {
                    linearLayout.setVisibility(View.INVISIBLE);
                    recyclerView.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    // by using live data we can observe modifications of database
//    private void fetchTasksFromDB() {
//        LiveData<List<Task>> tasksLiveData = database.taskDao().loadAllTasks();
//        tasksLiveData.observe(this, new Observer<List<Task>>() {
//            @Override
//            public void onChanged(List<Task> tasks) {
//                recyclerViewAdapter.setTasks(tasks);
//            }
//        });
//    }

    // by using executors we could do the db operations off the main thread
//    @Override
//    protected void onResume() {
//        super.onResume();
//        fetchTasksFromDB();
//    }
//
//    private void fetchTasksFromDB() {
//        AppExecutors.getInstance().diskIO().execute(new Runnable() {
//            @Override
//            public void run() {
//                List<Task> tasks = database.taskDao().loadAllTasks();
//
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        recyclerViewAdapter.setTasks(tasks);
//                    }
//                });
//            }
//        });
//    }

    // we can use this method but it's not recommended as its thread handling is not optimized
    //    @Override
//    protected void onResume() {
//        super.onResume();
//
//        Thread thread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                // database logic
//                List<Task> tasks = database.taskDao().loadAllTasks();
//                // updating UI (well can't be done here, it has to be done on main thread)
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        // updating UI
//                        recyclerViewAdapter.setTasks(tasks);
//                    }
//                });
//            }
//        });
//        thread.start();
//    }

    // this was possible if we could run db operations on main thread
//    @Override
//    protected void onResume() {
//        super.onResume();
//        recyclerViewAdapter.setTasks(database.taskDao().loadAllTasks());
//    }

    @Override
    public void onItemClickListener(int itemId) {
        // Launch AddTaskActivity adding the itemId as an extra in the intent
        Intent updateTaskIntent = new Intent(MainActivity.this, AddOrUpdateTaskActivity.class);
        updateTaskIntent.putExtra(AddOrUpdateTaskActivity.REQ_CODE_KEY, 2);
        updateTaskIntent.putExtra(AddOrUpdateTaskActivity.UPDATE_TASK_ID, itemId);
        startActivityForResult(updateTaskIntent, INTENT_REQ_CODE_UPDATE);
    }

}