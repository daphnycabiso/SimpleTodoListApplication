package com.example.jamesaljunsolijon.simpletodolistapp;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();

    private ArrayList<String> mList;
    private ArrayAdapter<String> mAdapter;
    private TextView mDateTimeTextView;
    private final int ADD_TASK_REQUEST = 1;
    private BroadcastReceiver mTickReceiver;
    private final String PREFS_TASKS = "prefs_tasks";
    private final String KEY_TASKS_LIST = "list";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 1
        super.onCreate(savedInstanceState);

        // 2 -Make the activity full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // 3
        setContentView(R.layout.activity_main);

        // 4
        mDateTimeTextView = (TextView) findViewById(R.id.dateTimeTextView);
        final Button addTaskBtn = (Button) findViewById(R.id.addTaskBtn);
        final ListView listview = (ListView) findViewById(R.id.taskListview);
        mList = new ArrayList<String>();
        String savedList = getSharedPreferences(PREFS_TASKS, MODE_PRIVATE).getString(KEY_TASKS_LIST, null);
        if (savedList != null) {
            String[] items = savedList.split(",");
            mList = new ArrayList<String>(Arrays.asList(items));
        }

        // 5
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mList);
        listview.setAdapter(mAdapter);

        // 6
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                taskSelected(i);
            }
        });
        mTickReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Intent.ACTION_TIME_TICK)) {
                    mDateTimeTextView.setText(getCurrentTimeStamp());
                }
            }
        };
    }
    @Override
    protected void onResume() {
        // 1
        super.onResume();
        // 2
        mDateTimeTextView.setText(getCurrentTimeStamp());
        // 3
        registerReceiver(mTickReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
    }

    @Override
    protected void onPause() {
        // 4
        super.onPause();
        // 5
        if (mTickReceiver != null) {
            try {
                unregisterReceiver(mTickReceiver);
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Timetick Receiver not registered", e);
            }
        }
    }
    @Override
    protected void onStop() {
        super.onStop();

        // Save all data which you want to persist.
        StringBuilder savedList = new StringBuilder();
        for (String s : mList) {
            savedList.append(s);
            savedList.append(",");
        }
        getSharedPreferences(PREFS_TASKS, MODE_PRIVATE).edit()
                .putString(KEY_TASKS_LIST, savedList.toString()).commit();
    }
    public void addTaskClicked(View view) {
        Intent intent = new Intent(MainActivity.this, TaskDescriptionActivity.class);
        startActivityForResult(intent, ADD_TASK_REQUEST);
    }

    private static String getCurrentTimeStamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");//dd/MM/yyyy
        Date now = new Date();
        String strDate = sdf.format(now);
        return strDate;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // 1 - Check which request you're responding to
        if (requestCode == ADD_TASK_REQUEST) {
            // 2 - Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // 3 - The user entered a task. Add a task to the list.
                String task = data.getStringExtra(TaskDescriptionActivity.EXTRA_TASK_DESCRIPTION);
                mList.add(task);
                // 4
                mAdapter.notifyDataSetChanged();
            }
        }
    }
    private void taskSelected(final int position) {
        // 1
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);

        // 2
        alertDialogBuilder.setTitle(R.string.alert_title);

        // 3
        alertDialogBuilder
                .setMessage(mList.get(position))
                .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mList.remove(position);
                        mAdapter.notifyDataSetChanged();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        // 4
        AlertDialog alertDialog = alertDialogBuilder.create();

        // 5
        alertDialog.show();
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
}
