package com.example.myaccountingapp;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NotificationService extends NotificationListenerService {

    private DatabaseHelper dbHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        // 初始化 DatabaseHelper
        dbHelper = new DatabaseHelper(getApplicationContext());
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        String id = Integer.toString(sbn.getId());
        String packageName = sbn.getPackageName();
        String notificationText = sbn.getNotification().extras.getString("android.text");

        //line一次都會發送兩個通知，一個id固定是16880000，一個id看是誰的訊息
        //line pay 錢包的id是1179082268
        if (notificationText != null && packageName.equals("jp.naver.line.android") ) {
            logPaymentInfo(id,packageName,notificationText);
        }
    }

    private void logPaymentInfo(String id ,String packageName, String notificationText) {
        Log.d("packageName",id);
        Log.d("packageName",packageName);
        Log.d("notificationService",notificationText);

        String amountPattern = "NT\\$ (\\d+)";
        String itemNamePattern = "商店名稱: (.+)";
        String amount = null;
        String itemName = null;

        Pattern pattern = Pattern.compile(amountPattern);
        Matcher matcher = pattern.matcher(notificationText);
        if (matcher.find()) {
            amount = matcher.group(1);
            Log.d("NotificationParser", "account=" + amount);
        }


        pattern = Pattern.compile(itemNamePattern);
        matcher = pattern.matcher(notificationText);
        if (matcher.find()) {
            itemName = matcher.group(1);
            Log.d("NotificationParser", "itemname=\"" + itemName + "\"");
        }

        if (amount != null && itemName != null && id.equals("16880000")) {
            int amountValue = Integer.valueOf(amount);
            long returnId = dbHelper.insertItemAndReturnId(getCurrentDate(),itemName,amountValue,"line pay 自動記帳");
        }

    }

    private String getCurrentDate() {
        // 獲取當前日期的邏輯，例如:
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
        return sdf.format(new Date());
    }
}
