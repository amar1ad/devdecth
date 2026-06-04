package com.ammarad.dictionary;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.ArrayList;
import java.util.HashMap;

public class DailyNotificationService extends Service {
    
    private Timer timer;
    private DataManager dataManager;
    private NotificationHelper notificationHelper;
    
    @Override
    public void onCreate() {
        super.onCreate();
        dataManager = new DataManager(this);
        notificationHelper = new NotificationHelper(this);
        scheduleDailyNotification();
    }
    
    private void scheduleDailyNotification() {
        timer = new Timer();
        
        // الوقت الأول: الساعة 9:00 صباحاً
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 9);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        
        long now = System.currentTimeMillis();
        long firstRun = calendar.getTimeInMillis();
        
        // التأكد من أن firstRun في المستقبل
        if (firstRun <= now) {
            firstRun = now + 6 * 60 * 60 * 1000; // إضافة 6 ساعات من الآن
        }
        
        long delay = firstRun - now;
        
        // التأكد من أن delay موجبة
        if (delay <= 0) {
            delay = 60000; // دقيقة واحدة كحد أدنى
        }
        
        long interval = 6 * 60 * 60 * 1000; // 6 ساعات = 4 مرات في اليوم
        
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                sendDailyNotification();
            }
        }, delay, interval);
    }
    
    private void sendDailyNotification() {
        ArrayList<HashMap<String, String>> allTerms = dataManager.getAllTerms();
        
        if (allTerms != null && !allTerms.isEmpty()) {
            int randomIndex = (int) (Math.random() * allTerms.size());
            HashMap<String, String> randomTerm = allTerms.get(randomIndex);
            
            String termName = randomTerm.get("term_ar");
            String termDefinition = randomTerm.get("definition_ar");
            
            if (termDefinition != null && termDefinition.length() > 80) {
                termDefinition = termDefinition.substring(0, 77) + "...";
            }
            
            notificationHelper.sendDailyTermNotification(termName, termDefinition);
        }
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}