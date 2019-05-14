package com.kotva.joe.todolist;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.kotva.joe.todolist.DatabaseDescription.ToDoList;

class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "ToDoList.db";
    private static final int DATABASE_VERSION = 3;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String CREATE_TODOLIST_TABLE =
                "CREATE TABLE " + ToDoList.TABLE_NAME + "(" +
                        ToDoList._ID + " integer primary key, " +
                        ToDoList.COLUMN_NAME + " Text, " +
                        ToDoList.COLUMN_MONTH + " Text, " +
                        ToDoList.COLUMN_DAY + " Text, " +
                        ToDoList.COLUMN_YEAR + " Text, " +
                        ToDoList.COLUMN_HOUR + " Text, " +
                        ToDoList.COLUMN_MINUTESTRING + " Text, " +
                        ToDoList.COLUMN_AMPM + " Text, " +
                        ToDoList.COLUMN_DETAILS + " Text)";
        db.execSQL(CREATE_TODOLIST_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + ToDoList.TABLE_NAME);
        onCreate(db);
    }

    public boolean addData(String name, String month, String day, String year, String hour, String minuteDetails, String ampm, String details){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues= new ContentValues();
        contentValues.put(ToDoList.COLUMN_NAME, name);
        contentValues.put(ToDoList.COLUMN_MONTH, month);
        contentValues.put(ToDoList.COLUMN_DAY, day);
        contentValues.put(ToDoList.COLUMN_YEAR, year);
        contentValues.put(ToDoList.COLUMN_HOUR, hour);
        contentValues.put(ToDoList.COLUMN_MINUTESTRING, minuteDetails);
        contentValues.put(ToDoList.COLUMN_AMPM, ampm);
        contentValues.put(ToDoList.COLUMN_DETAILS, details);


        long result = db.insert(ToDoList.TABLE_NAME, null, contentValues);
        if (result == -1) {
            return false;
        }
        else {
            return true;
        }
    }

    public Cursor showData(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor data = db.rawQuery("SELECT * FROM " + ToDoList.TABLE_NAME, null);
        return data;
    }

    public Integer deleteData(String taskName) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(ToDoList.TABLE_NAME, ToDoList.COLUMN_NAME + " = ?" , new String[] {taskName});
    }

}