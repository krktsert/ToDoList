package com.nextev.assignment.todolist;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.nextev.assignment.todolist.Database.DBConnector;
import com.nextev.assignment.todolist.Database.DBConnectorHelper;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    private ListView todoListView;
    private ArrayAdapter todoListArrayAdapter;
    private ArrayList<String> todoList = new ArrayList<>();

    private DBConnector todoListDbConnector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //DB Connection (required only once)
        if(todoListDbConnector != null) {
            //if there is already one connector
            //DO NOTING
        }else {
            todoListDbConnector = new DBConnector(this);
        }

        //List set Up (required only once)
        todoListArrayAdapter = new ArrayAdapter(this, R.layout.todo_list_view, R.id.task_title, todoList);

        todoListView = (ListView) findViewById(R.id.todolistView);

        todoListView.setAdapter(todoListArrayAdapter);

        registerForContextMenu(todoListView);
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
        int menuItemIndex = item.getItemId();
        String[] menuItems = getResources().getStringArray(R.array.menu);
        String menuItemName = menuItems[menuItemIndex];
        String listItemName = todoList.get(info.position);

        switch (item.getItemId()){
            case 0: //EDIT
                break;
            case 1: //DELETE
                deleteTask(listItemName);
                break;
            case 2: //SHARE
                final EditText taskEditText = new EditText(this);
                taskEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(30)});
                AlertDialog dialog = new AlertDialog.Builder(this)
                        .setTitle("Share the task")
                        .setMessage("Whom you want to send ?")
                        .setView(taskEditText)
                        .setPositiveButton("Share", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                shareTask(String.valueOf(taskEditText.getText()));
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .create();
                dialog.show();
                //shareTask();
                break;
        }

        Toast.makeText(this,"ITEM SELECTED " + menuItemName + " AND " + listItemName + " ITEM ID: " + item.getItemId(), Toast.LENGTH_SHORT).show();

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
                final EditText taskEditText = new EditText(this);
                taskEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(30)});
                AlertDialog dialog = new AlertDialog.Builder(this)
                        .setTitle("Add a new task")
                        .setMessage("What do you want to do next?")
                        .setView(taskEditText)
                        .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String task = String.valueOf(taskEditText.getText());
                                SQLiteDatabase db = todoListDbConnector.getWritableDatabase();
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

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //to refresh UI to update view elements
    public void updateView(){
        ArrayList<String> taskList = new ArrayList<>();
        SQLiteDatabase db = todoListDbConnector.getReadableDatabase();
        Cursor cursor = db.query(DBConnectorHelper.TaskEntry.TABLE,
                new String[]{DBConnectorHelper.TaskEntry._ID, DBConnectorHelper.TaskEntry.COL_TASK_TITLE},
                null, null, null, null, null);
        while (cursor.moveToNext()) {
            int idx = cursor.getColumnIndex(DBConnectorHelper.TaskEntry.COL_TASK_TITLE);
            taskList.add(cursor.getString(idx));
        }

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

    private void shareTask(String email){
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
        i.putExtra(Intent.EXTRA_SUBJECT, "I want to share my ToDo List with You");
        String extra_text = "I want to share my ToDo List with You. \n" +
                "Please download my ToDo List App via\n\n" +
                "the link\n\n"+
                "THANKS!";
        i.putExtra(Intent.EXTRA_TEXT   , extra_text);
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
