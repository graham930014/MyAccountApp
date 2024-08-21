package com.example.myaccountingapp;

public class Item {
    private long id;
    private String date;
    private String itemName;
    private int amount;
    private String note; //note可以為空字串

    public Item(long id, String date, String itemName, int amount, String note) {
        this.id = id;
        this.date = date;
        this.itemName = itemName;
        this.amount = amount;
        this.note = note != null ? note : "";
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note != null ? note : "";
    }

}
