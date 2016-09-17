package com.nextev.assignment.todolist;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;


import com.nextev.assignment.todolist.Database.DBConnector;
import com.nextev.assignment.todolist.Database.DBConnectorHelper;

import java.io.File;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {
    static final int PICK_FILE = 13;

    private ListView todoListView;
    private ArrayAdapter todoListArrayAdapter;
    private ArrayList<String> todoList = new ArrayList<>();

    private DBConnector todoListDbConnector;
    private String todoListDownloadedListPath = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //DB Connection (required only once)
        if(todoListDbConnector != null) {
            //if there is already one connector
            Toast.makeText(this, "NO Database connection.", Toast.LENGTH_SHORT).show();
        }else {
            todoListDbConnector = new DBConnector(this);
        }

        //List set Up (required only once)
        todoListArrayAdapter = new ArrayAdapter(this, R.layout.todo_list_view, R.id.task_title, todoList);
        todoListView = (ListView) findViewById(R.id.todolistView);
        todoListView.setAdapter(todoListArrayAdapter);

        //Log.d("DATABASE LOCATION", "HERE: "+ todoListDbConnector.getReadableDatabase().getPath());

        //Refresh everytime list page re-creates.
        todoListDownloadedListPath = null;

        registerForContextMenu(todoListView);

        //load initial items.
        updateView();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
       if (v.getId()==R.id.todolistView) {
            String[] menuItems = getResources().getStringArray(R.array.menu);
            for (int i = 0; i<menuItems.length; i++) {
                menu.add(Menu.NONE, i, i, menuItems[i]);
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        final String listItemName = todoList.get(info.position);

        switch (item.getItemId()){
            case 0: //EDIT
                //create a edit text with selected item's text
                final EditText taskEditText = new EditText(this);
                taskEditText.setText(listItemName);
                //set max length
                taskEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(30)});
                //alert dialog for editing
                AlertDialog dialogEdit = new AlertDialog.Builder(this)
                        .setTitle("Edit task")
                        .setMessage("What do you want to do next?")
                        .setView(taskEditText)
                        .setPositiveButton("Edit", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String task = String.valueOf(taskEditText.getText());
                                //if there is open downloaded list
                                SQLiteDatabase db;
                                if(todoListDownloadedListPath != null){
                                    //chose that one
                                    db = SQLiteDatabase.openDatabase(todoListDownloadedListPath, null, 0);
                                }else {
                                    //chose default
                                    db = todoListDbConnector.getReadableDatabase();
                                }
                                ContentValues values = new ContentValues();
                                values.put(DBConnectorHelper.TaskEntry.COL_TASK_TITLE, task);
                                db.execSQL("UPDATE " + DBConnectorHelper.TaskEntry.TABLE +
                                        " SET " + DBConnectorHelper.TaskEntry.COL_TASK_TITLE + "='"+task+"' "
                                        +"WHERE " + DBConnectorHelper.TaskEntry.COL_TASK_TITLE + "='" + listItemName +"'");

                                updateView();
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .create();
                dialogEdit.show();
                break;
            case 1: //DELETE
                deleteTask(listItemName);
                break;
        }


        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_task:
                //create a edit text
                final EditText taskEditText = new EditText(this);
                //set max length
                taskEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(30)});
                //alert dialog for adding
                AlertDialog dialog = new AlertDialog.Builder(this)
                        .setTitle("Add a new task")
                        .setMessage("What do you want to do next?")
                        .setView(taskEditText)
                        .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String task = String.valueOf(taskEditText.getText());
                                //if there is open downloaded list
                                SQLiteDatabase db;
                                if(todoListDownloadedListPath != null){
                                    //chose that one
                                    db = SQLiteDatabase.openDatabase(todoListDownloadedListPath, null, 0);
                                }else {
                                    //chose default
                                    db = todoListDbConnector.getReadableDatabase();
                                }
                                ContentValues values = new ContentValues();
                                values.put(DBConnectorHelper.TaskEntry.COL_TASK_TITLE, task);
                                db.insertWithOnConflict(DBConnectorHelper.TaskEntry.TABLE,
                                        null,
                                        values,
                                        SQLiteDatabase.CONFLICT_REPLACE);
                                db.close();
                                updateView();
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .create();
                dialog.show();
                return true;
            case R.id.action_share_task: //SHARE
                //create a edit text for e-mail address entry
                final EditText taskShareText = new EditText(this);
                //alert dialog for email entry
                AlertDialog dialogShare = new AlertDialog.Builder(this)
                        .setTitle("Share the task")
                        .setMessage("Enter email address:")
                        .setView(taskShareText)
                        .setPositiveButton("Share", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                shareTaskList(String.valueOf(taskShareText.getText()));
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .create();
                dialogShare.show();
                return true;
            case R.id.action_open_list: //Open downloaded list
                //default android file pick
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                startActivityForResult(intent, PICK_FILE);

                updateView();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //to refresh UI to update view elements
    public void updateView(){
        //Update the task list from DB
        ArrayList<String> taskList = new ArrayList<>();
        SQLiteDatabase db;
        if(todoListDownloadedListPath != null){
            db = SQLiteDatabase.openDatabase(todoListDownloadedListPath, null, 0);
        }else {
            db = todoListDbConnector.getReadableDatabase();
        }
        Cursor cursor = db.query(DBConnectorHelper.TaskEntry.TABLE,
                new String[]{DBConnectorHelper.TaskEntry._ID, DBConnectorHelper.TaskEntry.COL_TASK_TITLE},
                null, null, null, null, null);
        while (cursor.moveToNext()) {
            int idx = cursor.getColumnIndex(DBConnectorHelper.TaskEntry.COL_TASK_TITLE);
            taskList.add(cursor.getString(idx));
        }

        //update view element
        if (todoListArrayAdapter == null) {
            todoListArrayAdapter = new ArrayAdapter<>(this,
                    R.layout.todo_list_view,
                    R.id.task_title,
                    taskList);
            todoListView.setAdapter(todoListArrayAdapter);
        } else {
            todoListArrayAdapter.clear();
            todoListArrayAdapter.addAll(taskList);
            todoListArrayAdapter.notifyDataSetChanged();
        }

        cursor.close();
        db.close();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PICK_FILE){
            if(resultCode == RESULT_OK){
                String path = data.getData().getPath();
                todoListDownloadedListPath = path;
                updateView();
            }
        }
    }

    private void shareTaskList(String email){
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
        i.putExtra(Intent.EXTRA_SUBJECT, "I want to share my ToDo List with You");
        String extra_text = "I want to share my ToDo List with You. \n" +
                "Please download my ToDo List App via\n\n" +
                "https://github.com/krktsert/ToDoList\n\n"+
                "THANKS!";
        i.putExtra(Intent.EXTRA_TEXT   , extra_text);
        File file = new File(todoListDbConnector.getReadableDatabase().getPath());
        Uri data = FileProvider.getUriForFile(getApplicationContext(), "com.nextev.assignment.todolist.fileprovider", file);
        i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        i.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        i.putExtra(Intent.EXTRA_STREAM, data);

        try {
            startActivity(Intent.createChooser(i, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean deleteTask(String task){
        SQLiteDatabase db = todoListDbConnector.getWritableDatabase();
        db.delete(DBConnectorHelper.TaskEntry.TABLE,
                DBConnectorHelper.TaskEntry.COL_TASK_TITLE + " = ?",
                new String[]{task});
        db.close();
        updateView();
        return false;
    }

}
