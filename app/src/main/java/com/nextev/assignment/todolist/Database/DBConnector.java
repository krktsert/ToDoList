package com.nextev.assignment.todolist.Database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Korkut on 9/15/2016.
 */

public class DBConnector extends SQLiteOpenHelper {


    public DBConnector(Context context) {
        super(context, DBConnectorHelper.DB_NAME, null, DBConnectorHelper.DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String createTable = "CREATE TABLE " + DBConnectorHelper.TaskEntry.TABLE + " ( " +
                DBConnectorHelper.TaskEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                DBConnectorHelper.TaskEntry.COL_TASK_TITLE + " TEXT NOT NULL);";

        sqLiteDatabase.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldV, int newV) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + DBConnectorHelper.TaskEntry.TABLE);
        onCreate(sqLiteDatabase);
    }

    @Override
    public SQLiteDatabase getReadableDatabase() {
        return super.getReadableDatabase();
    }
}
