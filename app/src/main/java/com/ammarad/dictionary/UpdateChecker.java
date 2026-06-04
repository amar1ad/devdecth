package com.ammarad.dictionary;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class UpdateChecker {
    
    private Context context;
    private SharedPreferences prefs;
    
    public UpdateChecker(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences("app_data", Context.MODE_PRIVATE);
    }
    
    public void checkForUpdates() {
        Toast.makeText(context, "جاري التحقق...", Toast.LENGTH_SHORT).show();
        
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL("https://raw.githubusercontent.com/amar1ad/My_app/refs/heads/main/data.json");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(10000);
                    
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    reader.close();
                    connection.disconnect();
                    
                    final String jsonData = result.toString();
                    JSONObject fullObject = new JSONObject(jsonData);
                    final String newVersion = fullObject.optString("version", "1.0.0");
                    final String savedVersion = prefs.getString("last_version", "0");
                    
                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (!newVersion.equals(savedVersion)) {
                                prefs.edit().putString("last_version", newVersion).apply();
                                Toast.makeText(context, " تحديث جديد متاح! اضغط تحديث البيانات", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(context, " أنت تستخدم أحدث إصدار", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } catch (Exception e) {
                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, " فشل التحقق", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }
}