package com.example.myaccountingapp;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.EditText;
import android.util.Log;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.ArrayList;

public class ItemManager {
    private TreeMap<String, List<Item>> itemMap; //用date當成key
    private Context context;
    private LinearLayout tableContainer;
    private DatabaseHelper dbHelper;
    private PieChart pieChart;

    public ItemManager(Context context, LinearLayout tableContainer,PieChart pieChart, DatabaseHelper dbHelper) {
        this.context = context;
        this.tableContainer = tableContainer;
        this.pieChart = pieChart;
        this.itemMap = new TreeMap<>(Collections.reverseOrder());
        this.dbHelper = dbHelper;
    }

    public void addItem(Item item) {
        String date = item.getDate();

        if (!itemMap.containsKey(date)) {
            itemMap.put(date, new ArrayList<>());
        }
        itemMap.get(date).add(item);


        updateUI();
    }

    public void updateUI() {
        tableContainer.removeAllViews();

        for (Map.Entry<String, List<Item>> entry : itemMap.entrySet()) {
            String date = entry.getKey();
            List<Item> items = entry.getValue();

            //建立一個日期的table
            TableLayout tableLayout = new TableLayout(context);
            tableLayout.setPadding(10, 10, 10, 10); // 增加padding
            tableLayout.setBackgroundColor(Color.parseColor("#D1E7F5")); // 設置黑色邊框

            TextView dateHeader = new TextView(context);
            dateHeader.setText(date);
            dateHeader.setTextSize(18);
            dateHeader.setPadding(5, 5, 5, 5);
            dateHeader.setBackgroundColor(Color.parseColor("#FADADD"));
            dateHeader.setTextColor(Color.parseColor("#4A4A4A"));
            tableLayout.addView(dateHeader);

            View blackLine = new View(context);
            blackLine.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, 2));
            blackLine.setBackgroundColor(Color.BLACK);
            tableLayout.addView(blackLine);

            tableContainer.addView(tableLayout);

            for (Item item : items) {
                TableRow row = new TableRow(context);
                row.setPadding(5, 5, 5, 5); // 增加padding

                TextView itemName = new TextView(context);
                TextView itemAmount = new TextView(context);
                TextView itemNote = new TextView(context);

                itemName.setText(item.getItemName());
                itemAmount.setText("$ " + String.valueOf(item.getAmount()));
                itemNote.setText(item.getNote());

                itemName.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f));
                itemNote.setTextColor(Color.GRAY); // 設置灰色文字
                itemNote.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f));
                itemNote.setPadding(10, 0, 10, 0); // 增加padding
                itemAmount.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f));
                itemAmount.setGravity(Gravity.END); // 將金額靠右對齊

                row.addView(itemName);
                row.addView(itemNote);
                row.addView(itemAmount);


                //點擊時顯示編輯或刪除物品的框框
                row.setOnClickListener(v -> {
                    showEditDialog(item);
                });

                tableLayout.addView(row);


                // 在每個item之間添加灰色的分隔線
                View grayLine = new View(context);
                grayLine.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, 1));
                grayLine.setBackgroundColor(Color.GRAY);
                tableLayout.addView(grayLine);
            }

        }
        updatePieChart();//也順便更新pieChart
    }

    public void showEditDialog(Item item){
        Log.d("showEditDialog","ID: " + item.getId() + ", Date: " + item.getDate()
                + ", Item: " + item.getItemName() + ", Amount: " + item.getAmount() + ", Note: " + item.getNote() );

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_item, null);

        builder.setView(dialogView);

        EditText editDate = dialogView.findViewById(R.id.dialogEditTextDate);
        EditText editName = dialogView.findViewById(R.id.dialogEditTextName);
        EditText editAmount = dialogView.findViewById(R.id.dialogEditTextAmount);
        EditText editNote = dialogView.findViewById(R.id.dialogeditTextNote);

        editDate.setText(item.getDate());
        editName.setText(item.getItemName());
        editAmount.setText(String.valueOf(item.getAmount()));
        editNote.setText(item.getNote());

        builder.setTitle("Edit Item")
                .setPositiveButton("Save", (dialog, which) -> {
                    // 獲取用戶輸入的數據
                    String date = editDate.getText().toString();
                    String name = editName.getText().toString();
                    int amount = Integer.parseInt(editAmount.getText().toString());
                    String note = editNote.getText().toString();


                    updateItem(item,date,name,amount,note);
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setNeutralButton("Delete", (dialog, which) -> deleteItem(item))
                .create()
                .show();

    }

    public void updateItem(Item item, String date, String itemName, int amount, String note){
        item.setDate(date);
        item.setItemName(itemName);
        item.setAmount(amount);
        item.setNote(note);

        //保存到資料庫
        dbHelper.updateItem(item);

        updateUI();
    }

    public void deleteItem(Item item){
        String key = item.getDate();
        if (itemMap.containsKey(key)) {
            List<Item> items = itemMap.get(key);
            items.remove(item);

            if (items.isEmpty()) {
                itemMap.remove(key);
            }

            dbHelper.deleteItem(item);
        }

        updateUI();
    }

    public void addItems(List<Item> items){
        for(Item item: items){
            String date = item.getDate();

            if (!itemMap.containsKey(date)) {
                itemMap.put(date, new ArrayList<>());
            }
            itemMap.get(date).add(item);
        }

        updateUI();
    }

    public void clearAllItem(){
        itemMap.clear();
    }

    public void updatePieChart(){

        ArrayList<PieEntry> entries = new ArrayList<>();
        TreeMap<String, Integer> itemTotalAmount = new TreeMap<>();

        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.parseColor("#304567"));
        colors.add(Color.parseColor("#309967"));
        colors.add(Color.parseColor("#476567"));
        colors.add(Color.parseColor("#890567"));
        colors.add(Color.parseColor("#a35567"));
        colors.add(Color.parseColor("#ff5f67"));
        colors.add(Color.parseColor("#3ca567"));

        int totalAmount=0;
        for (Map.Entry<String, List<Item>> entry : itemMap.entrySet()) {
            String date = entry.getKey();
            List<Item> items = entry.getValue();
            for(Item item : items){
                String itemName = item.getItemName();
                int amount = item.getAmount();
                if(itemTotalAmount.containsKey(itemName)){
                    itemTotalAmount.put(itemName, itemTotalAmount.get(itemName) + amount);
                }
                else{
                    itemTotalAmount.put(itemName, amount);
                }
                totalAmount += item.getAmount();
                //entries.add(new PieEntry(item.getAmount(), item.getItemName()));

            }

        }

        for(Map.Entry<String, Integer> entry : itemTotalAmount.entrySet()){
            entries.add(new PieEntry(entry.getValue(), entry.getKey()));
        }

        ValueFormatter dollarFormatter = new ValueFormatter() {
            @Override
            public String getPieLabel(float value, PieEntry pieEntry) {
                return "$ " + String.valueOf((int) value); // 格式化為整數並加上 $
            }
        };

        PieDataSet dataSet = new PieDataSet(entries,"支出統計");
        dataSet.setColors(colors);
        dataSet.setValueTextSize(14f);
        PieData data = new PieData(dataSet);
        data.setValueFormatter(dollarFormatter);
        pieChart.setData(data);
        pieChart.setUsePercentValues(false);
        pieChart.getDescription().setEnabled(false);
        pieChart.setCenterText("總支出:\n$ "+Integer.toString(totalAmount)); // 中心文本
        pieChart.animateY(1000); // 添加動畫
        pieChart.invalidate(); // 刷新圖表

    }
}
