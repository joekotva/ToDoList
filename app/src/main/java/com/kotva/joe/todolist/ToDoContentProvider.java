package com.kotva.joe.todolist;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import com.kotva.joe.todolist.DatabaseDescription.ToDoList;

public class ToDoContentProvider extends ContentProvider {

    private DatabaseHelper dbHelper;

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    private static final int ITEM = 1;
    private static final int LIST = 2;

    static {
        uriMatcher.addURI(DatabaseDescription.AUTHORITY, ToDoList.TABLE_NAME + "/#", ITEM);

        uriMatcher.addURI(DatabaseDescription.AUTHORITY, ToDoList.TABLE_NAME, LIST);
    }

    @Override
    public boolean onCreate() {
        dbHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(ToDoList.TABLE_NAME);

        switch (uriMatcher.match(uri)) {
            case ITEM:
                queryBuilder.appendWhere(ToDoList._ID + "=" + uri.getLastPathSegment());
                break;
            case LIST:
                break;
            default:
                throw new UnsupportedOperationException(getContext().getString(R.string.invalid_query_uri) + uri);
        }

        Cursor cursor = queryBuilder.query(dbHelper.getReadableDatabase(), projection, selection, selectionArgs, null, null, sortOrder);

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Uri newTaskUri = null;

        switch (uriMatcher.match(uri)) {
            case ITEM:
                long rowId = dbHelper.getWritableDatabase().insert(ToDoList.TABLE_NAME, null, values);

                if (rowId > 0) {
                    newTaskUri = ToDoList.buildContactUri(rowId);

                    getContext().getContentResolver().notifyChange(uri, null);
                }
                else
                    throw new SQLException(getContext().getString(R.string.insert_failed) + uri);
                break;
            default:
                throw new UnsupportedOperationException(getContext().getString(R.string.invalid_insert_uri) + uri);
        }

        return newTaskUri;
    }

    @Override
    public int update(Uri uri,ContentValues values,String selection,String[] selectionArgs) {
        int numberOfRowsUpdated;

        switch (uriMatcher.match(uri)) {
            case ITEM:

                String id = uri.getLastPathSegment();

                numberOfRowsUpdated = dbHelper.getWritableDatabase().update(ToDoList.TABLE_NAME, values, ToDoList._ID + "=" + id, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException(getContext().getString(R.string.invalid_update_uri) + uri);
        }

        if (numberOfRowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return numberOfRowsUpdated;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int numberOfRowsDeleted;

        switch (uriMatcher.match(uri)) {
            case ITEM:

                String id = uri.getLastPathSegment();

                numberOfRowsDeleted = dbHelper.getWritableDatabase().delete(ToDoList.TABLE_NAME, ToDoList._ID + "=" + id, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException(getContext().getString(R.string.invalid_delete_uri) + uri);
        }

        if (numberOfRowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return numberOfRowsDeleted;
    }
}
