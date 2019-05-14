package com.kotva.joe.todolist;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

public class DatabaseDescription {

    public static final String AUTHORITY = "com.kotva.joe.todolist";

    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    public static final class ToDoList implements BaseColumns {
        public static final String TABLE_NAME = "ToDoList";

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(TABLE_NAME).build();

        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_MONTH= "month";
        public static final String COLUMN_DAY= "day";
        public static final String COLUMN_YEAR= "year";
        public static final String COLUMN_HOUR= "hour";
        public static final String COLUMN_MINUTESTRING= "MINUTESTRING";
        public static final String COLUMN_AMPM= "AMPM";
        public static final String COLUMN_DETAILS = "details";

        public static Uri buildContactUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }
}
