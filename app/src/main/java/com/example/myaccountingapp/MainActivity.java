package com.example.myaccountingapp;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.provider.Settings;
import android.util.Log;
import android.widget.EditText;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;

import android.app.DatePickerDialog;
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class MainActivity extends AppCompatActivity {
    private EditText editTextDate, editTextName, editTextAmount, editTextNote;
    private Button addButton;
    private LinearLayout tableContainer;
    private final Calendar calendar = Calendar.getInstance();
    private Map<String, TableLayout> tableMap;
    private ItemManager itemManager;
    private DatabaseHelper dbHelper;
    private TextView monthTextView;
    private ImageView leftArrow, rightArrow;
    private String currentDisplayMonth;
    private PieChart pieChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        if (!isNotificationServiceEnabled()) {
            // 引導用戶開啟通知權限
            requestNotificationPermission();
        }

        editTextDate = findViewById(R.id.editTextDate);
        editTextName = findViewById(R.id.editTextName);
        editTextAmount = findViewById(R.id.editTextAmount);
        editTextNote = findViewById(R.id.editTextNote);
        addButton = findViewById(R.id.addButton);
        tableContainer = findViewById(R.id.tableContainer);
        dbHelper = new DatabaseHelper(this);
        PieChart pieChart = findViewById(R.id.pieChart);
        itemManager = new ItemManager(this, tableContainer,pieChart, dbHelper);
        monthTextView = findViewById(R.id.monthTextView);
        leftArrow = findViewById(R.id.leftArrow);
        rightArrow = findViewById(R.id.rightArrow);



        String currentDate = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(calendar.getTime());
        editTextDate.setText(currentDate);
        editTextDate.setOnClickListener(v -> pickCalendar());

        addButton.setOnClickListener(v -> addItemToTable());

        //讀取當月份資料
        Log.d("MainActivity", "onCreate called");
        currentDisplayMonth = new SimpleDateFormat("yyyy/MM", Locale.getDefault()).format(calendar.getTime());

        updateMonthDisplay();

        leftArrow.setOnClickListener(v -> {
            String[] parts = currentDisplayMonth.split("/");
            int year = Integer.valueOf(parts[0]);
            int month = Integer.valueOf(parts[1]) - 1;
            currentDisplayMonth = String.format(Locale.getDefault(), "%d/%02d", year, month);
            updateMonthDisplay();
        });

        rightArrow.setOnClickListener(v -> {
            String[] parts = currentDisplayMonth.split("/");
            int year = Integer.valueOf(parts[0]);
            int month = Integer.valueOf(parts[1]) + 1;
            currentDisplayMonth = String.format(Locale.getDefault(), "%d/%02d", year, month);
            updateMonthDisplay();
        });


    }

    private void updateMonthDisplay() {
        String[] parts = currentDisplayMonth.split("/");
        int year = Integer.valueOf(parts[0]) ;
        int month = Integer.valueOf(parts[1]) ;
        String formattedMonth = String.format(Locale.getDefault(), "%d年%02d月", year, month);
        monthTextView.setText(formattedMonth);

        loadMonthData();

    }

    private boolean isNotificationServiceEnabled() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //String pkgName = getPackageName();
            android.app.NotificationManager notificationManager = (android.app.NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            return notificationManager.isNotificationPolicyAccessGranted();
        }
        return true;
    }
    private void requestNotificationPermission() {
        Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
        startActivity(intent);
    }

    private void pickCalendar(){
        DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this,(view, year, monthOfYear, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, monthOfYear);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateLabel();
        },calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void updateLabel() {
        String myFormat = "yyyy/MM/dd"; // Date format
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.getDefault());
        editTextDate.setText(sdf.format(calendar.getTime()));
    }

    private void addItemToTable(){
        String date = editTextDate.getText().toString();
        String name = editTextName.getText().toString();
        String amount = editTextAmount.getText().toString();
        String note = editTextNote.getText().toString();
        if (date.isEmpty() || name.isEmpty() || amount.isEmpty()){
            return ;
        }
        int amountValue =  Integer.parseInt(amount);



        long id = dbHelper.insertItemAndReturnId(date, name, amountValue, note);
        Item item = new Item(id, date, name, amountValue, note);
        //如果是當月的才加到itemManager
        String[] parts = date.split("/");
        int year = Integer.valueOf(parts[0]);
        int month = Integer.valueOf(parts[1]);
        String dateYearAndMonth = String.format(Locale.getDefault(), "%d/%02d", year, month);
        if(dateYearAndMonth.equals(currentDisplayMonth)){
            itemManager.addItem(item);
        }



        editTextNote.setText("");
        editTextName.setText("");
        editTextAmount.setText("");

    }

    private void loadMonthData(){
        List<Item> items = new ArrayList<>();
        items = dbHelper.loadMonthData(currentDisplayMonth);
        itemManager.clearAllItem();
        itemManager.addItems(items);

    }
}