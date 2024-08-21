package com.example.myaccountingapp;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.Cursor;
import android.content.Context;
import android.content.ContentValues;
import android.util.Log;

import java.util.List;
import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper{
    private static final String DATABASE_NAME = "accounting.db";
    private static final int DATABASE_VERSION = 2;

    public static final String TABLE_NAME = "item";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_ITEM_NAME = "item_name";
    public static final String COLUMN_AMOUNT = "amount";
    public static final String COLUMN_NOTE = "note";

    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_DATE + " TEXT, " +
                    COLUMN_ITEM_NAME + " TEXT, " +
                    COLUMN_AMOUNT + " INTEGER, " +
                    COLUMN_NOTE + " TEXT);";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 如果沒有table創建table
        db.execSQL(TABLE_CREATE);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 如果table變更，drop掉重建
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public long insertItemAndReturnId(String date, String itemName, int amount, String note){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_DATE, date);
        values.put(COLUMN_ITEM_NAME, itemName);
        values.put(COLUMN_AMOUNT, amount);
        values.put(COLUMN_NOTE, note.isEmpty() ? null : note);

        // 回傳id
        long id = db.insert(TABLE_NAME, null, values);
        db.close();
        return id;
    }

    public void updateItem(Item item){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_DATE, item.getDate());
        values.put(COLUMN_ITEM_NAME, item.getItemName());
        values.put(COLUMN_AMOUNT, item.getAmount());
        values.put(COLUMN_NOTE, item.getNote());

        int rowsAffected = db.update(TABLE_NAME, values, COLUMN_ID + " = ?", new String[]{String.valueOf(item.getId())});
        db.close();
    }

    public void deleteItem(Item item){
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsDeleted = db.delete(TABLE_NAME, COLUMN_ID + " = ?", new String[]{String.valueOf(item.getId())});
        db.close();
    }

    public List<Item> loadMonthData(String month){
        List<Item> items = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                this.TABLE_NAME,
                null,
                 this.COLUMN_DATE + " like ?",
                new String[]{month+"%"},
                null, null, null);

        if (cursor != null) {
            try {
                int idColumnIndex = cursor.getColumnIndexOrThrow(COLUMN_ID);
                int dateColumnIndex = cursor.getColumnIndexOrThrow(COLUMN_DATE);
                int itemNameColumnIndex = cursor.getColumnIndexOrThrow(COLUMN_ITEM_NAME);
                int amountColumnIndex = cursor.getColumnIndexOrThrow(COLUMN_AMOUNT);
                int noteColumnIndex = cursor.getColumnIndexOrThrow(COLUMN_NOTE);

                while (cursor.moveToNext()) {
                    int id = cursor.getInt(idColumnIndex);
                    String date = cursor.getString(dateColumnIndex);
                    String itemName = cursor.getString(itemNameColumnIndex);
                    int amount = cursor.getInt(amountColumnIndex);
                    String note = cursor.getString(noteColumnIndex);


                    items.add(new Item(id, date, itemName, amount, note));
                }
            }catch (Exception e) {
                //有異常
                Log.e("DatabaseHelper", "Error loading month data", e);
            } finally {
                cursor.close();
            }
        }else {
            Log.d("DatabaseHelper", "Cursor is null, no data found for month: " + month);
        }
        Log.d("DatabaseHelper","loadMonthDataCalled");
        return items;
    }
}
