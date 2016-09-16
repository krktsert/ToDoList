package com.nextev.assignment.todolist.Database;

import android.provider.BaseColumns;

/**
 * Created by Korkut on 9/15/2016.
 */
public class DBConnectorHelper {
    public static final String DB_NAME = "com.nextev.assignment.todolist.db";
    public static final int DB_VERSION = 1;

    public class TaskEntry implements BaseColumns {
        public static final String TABLE = "tasks";

        public static final String COL_TASK_TITLE = "title";
    }
}
