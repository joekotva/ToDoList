package com.kotva.joe.todolist;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private ArrayList<String> items;
    private ArrayAdapter<String> itemsAdapter;
    private ListView lvItems;
    private DatePickerDialog.OnDateSetListener myDateSetListener;
    private TimePickerDialog.OnTimeSetListener myTimeSetListener;
    private AlertDialog.Builder myDetailListener;
    private ArrayList<String> itemDetails;
    private ArrayList<String> taskNames;

    int year;
    int month;
    int day;
    int hour;
    int minute;

    int position;

    private String itemText;
    private String m_Text;

    private AlarmManager alarmManager;
    private PendingIntent alarmIntent;

    DatabaseHelper listDB;

    private String[] myDataArray;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Log.d(TAG, "test log");

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setImageResource(R.drawable.pink_plus);

        lvItems = (ListView) findViewById(R.id.lvItems);
        items = new ArrayList<String>();
        itemsAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, items);
        lvItems.setAdapter(itemsAdapter);
        items.add("");  // not sure why first item added doesn't display.
        // Possibly something wrong with GUI?
        // Fixed by manually adding a ToDo list item first

        itemDetails = new ArrayList<String>();
        taskNames = new ArrayList<String>();

        // set alarm to daily remind user of tasks
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 7);
        calendar.set(Calendar.MINUTE, 45);

        Intent intent = new Intent(getApplicationContext(), Notification_receiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 100, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);

        // database stuff
        listDB = new DatabaseHelper(this);


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                EditText etNewItem = (EditText) findViewById(R.id.etNewItem);
                itemText = etNewItem.getText().toString();
                etNewItem.setText("");

                setupListViewListener(); // add long click listener to our list item


                // end of entering task details

                // calender used to default to current date
                Calendar cal = Calendar.getInstance();
                year = cal.get(Calendar.YEAR);
                month = cal.get(Calendar.MONTH);
                day = cal.get(Calendar.DAY_OF_MONTH);
                hour = cal.get(Calendar.HOUR_OF_DAY);
                minute = cal.get(Calendar.MINUTE);

                DatePickerDialog dateDialog = new DatePickerDialog(
                        MainActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        myDateSetListener,
                        year, month, day
                );

                TimePickerDialog timeDialog = new TimePickerDialog(
                        MainActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        myTimeSetListener,
                        hour,
                        minute,
                        false
                );

                timeDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                timeDialog.show();

                dateDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dateDialog.show();


                // Entering in task details
                m_Text = "";

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Enter task details");

                // Set up the input
                final EditText input = new EditText(MainActivity.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);

                // Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        m_Text = input.getText().toString();
                        itemDetails.add(m_Text);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();

            }
        });

        myDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int y, int m, int d) {
                year = y;
                month = m;
                day = d;
            }
        };

        myTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int h, int m) {
                hour = h;
                minute = m;

                // logic to format date and time correctly
                String minuteString;
                if (minute < 10)
                    minuteString = '0' + Integer.toString(minute);
                else
                    minuteString = Integer.toString(minute);

                String ampm;
                if (hour < 12)
                    ampm = "am";
                else
                    ampm = "pm";
                if (hour == 0)  // not sure why the math to convert hour "0" to show "12" doesn't work, but this works
                    hour = 12;
                Log.d(TAG, "Hour selected: " + String.valueOf(hour));
                itemsAdapter.add(itemText + " Due: " + (month + 1) + '/' + day + '/' + year + ", " + ((hour - 1) % 12 + 1) + ':' + minuteString + ampm);

                listDB.addData(itemText, String.valueOf(month), String.valueOf(day), String.valueOf(year), String.valueOf(hour), minuteString, ampm, m_Text);

                taskNames.add(itemText);
            }
        };

//         Look at individual rows of data
        myDataArray = extractData();
//        for (String str : myDataArray) {
//            display("Data", str);
//            itemsAdapter.add(myDataArray[1] + " Due: " + (myDataArray[2] + 1) + '/' + myDataArray[3] + '/' + myDataArray[4] + ", " + ((Integer.parseInt(myDataArray[5]) - 1) % 12 + 1) + ':' + myDataArray[6] + myDataArray[7]);
//        }

//        listDB.deleteData("name");

        Cursor data = listDB.showData();
        StringBuffer buffer = new StringBuffer();
        while (data.moveToNext()) {
            buffer.append("ID: " + data.getString(0) + "\n");
            buffer.append("Name: " + data.getString(1) + "\n");
            buffer.append("Month: " + data.getString(2) + "\n");
            buffer.append("Day: " + data.getString(3) + "\n");
            buffer.append("Year: " + data.getString(4) + "\n");
            buffer.append("Hour: " + data.getString(5) + "\n");
            buffer.append("Minute: " + data.getString(6) + "\n");
            buffer.append("AMPM: " + data.getString(7) + "\n");
            buffer.append("Details: " + data.getString(8) + "\n");
        }

//    display("All data", buffer.toString());
}

    private void display(String title, String message) {
        AlertDialog.Builder builder = new Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.show();
    }

    private String[] extractData() {
        Cursor data = listDB.showData();
        String array[] = new String[data.getCount()];
        int i = 0;

        data.moveToFirst();
        while (!data.isAfterLast()) {
            array[i] = data.getString(0) + data.getString(1) + data.getString(2) + data.getString(3) + data.getString(4) + data.getString(5) + data.getString(6) + data.getString(7) + data.getString(8);
            itemsAdapter.add(data.getString(1) + " Due: " + (data.getString(2) + 1) + '/' + data.getString(3) + '/' + data.getString(4) + ", " + ((Integer.parseInt(data.getString(5)) - 1) % 12 + 1) + ':' + data.getString(6) + data.getString(7));
            itemDetails.add(data.getString(8));
            taskNames.add(data.getString(1));
            i++;
            data.moveToNext();
        }
        setupListViewListener();

        return array;
    }

    // set up listeners for list items
    private void setupListViewListener() {

        // long click listener
        lvItems.setOnItemLongClickListener(
                new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> adapter,
                                                   View item, int pos, long id) {

                        position = pos;

                        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                        alertDialog.setTitle("Delete Task?");
                        alertDialog.setMessage(items.get(pos));
                        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Delete",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        items.remove(position);
                                        itemDetails.remove(position - 1);
                                        itemsAdapter.notifyDataSetChanged();
                                        String taskName = taskNames.get(position - 1);
                                        taskNames.remove(position - 1);
                                        Integer integer = listDB.deleteData(taskName);
//                                        Toast.makeText(MainActivity.this, "Tasks deleted: " + integer, Toast.LENGTH_SHORT).show();
                                        Toast.makeText(MainActivity.this, "Task '" + taskName + "' deleted", Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                    }
                                });
                        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });

                        alertDialog.show();

                        return true;
                    }

                });

        // regular click listener
        lvItems.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapter,
                                            View item, int pos, long id) {

                        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                        alertDialog.setTitle("Task Details");
                        alertDialog.setMessage(items.get(pos));
                        alertDialog.setMessage(itemDetails.get(pos - 1));
                        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                        alertDialog.show();

                        itemsAdapter.notifyDataSetChanged();

                    }

                });

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

//        private ArrayList<String> items;
//        private ArrayAdapter<String> itemsAdapter;
//        private ListView lvItems;
//        private DatePickerDialog.OnDateSetListener myDateSetListener;
//        private TimePickerDialog.OnTimeSetListener myTimeSetListener;
//        private ArrayList<String> itemDetails;

        outState.putStringArrayList("Items", items);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }
}
