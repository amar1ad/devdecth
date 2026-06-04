package com.ammarad.dictionary;

import com.ammarad.dictionary.SplashActivity;
import android.animation.*;
import android.app.*;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.*;
import android.content.res.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.media.*;
import android.net.*;
import android.os.*;
import android.text.*;
import android.text.style.*;
import android.util.*;
import android.view.*;
import android.view.View.*;
import android.view.animation.*;
import android.webkit.*;
import android.widget.*;
import com.ammarad.dictionary.databinding.*;
import java.io.*;
import java.text.*;
import java.util.*;
import java.util.regex.*;
import org.json.*;
import android.content.DialogInterface;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.content.pm.PackageManager;
import android.util.Log;

public class MainActivity extends Activity {
    
    private MainBinding binding;
    
    @Override
    protected void onCreate(Bundle _savedInstanceState) {
        super.onCreate(_savedInstanceState);
        binding = MainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initialize(_savedInstanceState);
        initializeLogic();
    }
    
    private void initialize(Bundle _savedInstanceState) {
    }
    
    private void initializeLogic() {
        // تهيئة العناصر
        gridView = findViewById(R.id.gridview1);
        searchView = findViewById(R.id.search_view);
        progressBar = findViewById(R.id.progressBar);
        btnRefreshData = findViewById(R.id.btn_refresh_data);
        btnCheckUpdate = findViewById(R.id.btn_check_update);
        btnLanguage = findViewById(R.id.btn_language);
        btnShare = findViewById(R.id.btn_share);
        statsCategories = findViewById(R.id.stats_categories);
        statsTerms = findViewById(R.id.stats_terms);
        statsLastUpdate = findViewById(R.id.stats_last_update);
        btnNotifications = findViewById(R.id.btn_notifications);
        
        // تهيئة مدير البيانات
        dataManager = new DataManager(this);
        gridAdapter = new GridAdapter(this, dataManager.getCategories(), isArabic);
        gridView.setAdapter(gridAdapter);
        
        // تحميل البيانات
        loadData();
        
        // إعداد الأزرار
        setupButtons();
        
        // إعداد البحث
        setupSearch();
        
        // بدء الإشعارات اليومية
        startDailyNotificationService();
        
        // زر تشغيل/إيقاف الإشعارات
        btnNotifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notificationsEnabled = !notificationsEnabled;
                if (notificationsEnabled) {
                    startDailyNotificationService();
                    btnNotifications.setText("🔔");
                    Toast.makeText(MainActivity.this, "تم تفعيل الإشعارات اليومية", Toast.LENGTH_SHORT).show();
                } else {
                    stopDailyNotificationService();
                    btnNotifications.setText("🔕");
                    Toast.makeText(MainActivity.this, "تم إيقاف الإشعارات اليومية", Toast.LENGTH_SHORT).show();
                }
            }
        });
        
        Button btnAbout = findViewById(R.id.btn_about);
        btnAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AboutActivity.class);
                startActivity(intent);
            }
        });
        
        Button btnLanguages = findViewById(R.id.btn_languages);
        btnLanguages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LanguagesActivity.class);
                startActivity(intent);
            }
        });
        
        // تهيئة ImageLoader
        ImageLoader.getInstance().initCacheDir(this);
        
        // التحقق من صحة التوقيع (الحماية من الهندسة العكسية)
        if (!isSignatureValid()) {
            Toast.makeText(this, "❌ هذا التطبيق غير رسمي. تم التعديل عليه.", Toast.LENGTH_LONG).show();
            android.os.Process.killProcess(android.os.Process.myPid());
        }
    }
    
    // تعريف المتغيرات
    private GridView gridView;
    private SearchView searchView;
    private ProgressBar progressBar;
    private Button btnRefreshData;
    private Button btnCheckUpdate;
    private Button btnLanguage;
    private ImageButton btnShare;
    private TextView statsCategories;
    private TextView statsTerms;
    private TextView statsLastUpdate;
    
    private DataManager dataManager;
    private GridAdapter gridAdapter;
    private boolean isArabic = true;
    private Button btnNotifications;
    private boolean notificationsEnabled = true;
    
    // استبدل دوال loadData(), fetchDataFromServer(), updateStats() بهذه النسخ
    private void loadData() {
        if (dataManager.hasLocalData()) {
            gridAdapter.updateData(dataManager.getCategories());
            updateStats();
            Toast.makeText(this, "📁 تم تحميل البيانات المخزنة", Toast.LENGTH_SHORT).show();
        } else {
            fetchDataFromServer();
        }
    }
    
    private void updateStats() {
        statsCategories.setText(String.valueOf(dataManager.getCategories().size()));
        statsTerms.setText(String.valueOf(dataManager.getTotalTerms()));
        statsLastUpdate.setText(dataManager.getLastUpdateTime());
    }
    
    private void fetchDataFromServer() {
        progressBar.setVisibility(ProgressBar.VISIBLE);
        
        dataManager.fetchAllData(new DataManager.DataCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        gridAdapter.updateData(dataManager.getCategories());
                        updateStats();
                        progressBar.setVisibility(ProgressBar.GONE);
                        Toast.makeText(MainActivity.this, "تم تحديث البيانات", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            
            @Override
            public void onFailure(final String error) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(ProgressBar.GONE);
                        Toast.makeText(MainActivity.this, error, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }
    
    private void setupButtons() {
        btnRefreshData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(ProgressBar.VISIBLE);
                dataManager.refreshData(new DataManager.DataCallback() {
                    @Override
                    public void onSuccess() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                gridAdapter.updateData(dataManager.getCategories());
                                updateStats();
                                progressBar.setVisibility(ProgressBar.GONE);
                                Toast.makeText(MainActivity.this, "تم تحديث البيانات", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    
                    @Override
                    public void onFailure(final String error) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setVisibility(ProgressBar.GONE);
                                Toast.makeText(MainActivity.this, error, Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                });
            }
        });
        
        btnCheckUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateChecker checker = new UpdateChecker(MainActivity.this);
                checker.checkForUpdates();
            }
        });
        
        btnLanguage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isArabic = !isArabic;
                btnLanguage.setText(isArabic ? "ar" : "en");
                gridAdapter.setLanguage(isArabic);
                if (searchView.getQuery().length() > 0) {
                    filterCategories(searchView.getQuery().toString());
                }
            }
        });
        
        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.shareApp(MainActivity.this, dataManager.getCategories().size());
            }
        });
        
        Button btnFavorites = findViewById(R.id.btn_favorites);
        btnFavorites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, FavoritesActivity.class);
                startActivity(intent);
            }
        });
    }
    
    private void setupSearch() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
            
            @Override
            public boolean onQueryTextChange(String newText) {
                filterCategories(newText);
                return true;
            }
        });
    }
    
    private void filterCategories(String query) {
        gridAdapter.filter(query);
    }
    
    // بدء خدمة الإشعارات اليومية
    private void startDailyNotificationService() {
        Intent serviceIntent = new Intent(this, DailyNotificationService.class);
        startService(serviceIntent);
    }
    
    // إيقاف خدمة الإشعارات اليومية
    private void stopDailyNotificationService() {
        Intent serviceIntent = new Intent(this, DailyNotificationService.class);
        stopService(serviceIntent);
    }
    
    // التحقق من صحة توقيع التطبيق (مقاومة الهندسة العكسية)
    private boolean isSignatureValid() {
        try {
            String originalSignature = "308204a830820390a003020102020900936eacbe07f201df300d06092a864886f70d0101050500308194310b3009060355040613025553311330110603550408130a43616c69666f726e6961311630140603550407130d4d6f756e7461696e20566965773110300e060355040a1307416e64726f69643110300e060355040b1307416e64726f69643110300e06035504031307416e64726f69643122302006092a864886f70d0109011613616e64726f696440616e64726f69642e636f6d301e170d3038303232393031333334365a170d3335303731373031333334365a308194310b3009060355040613025553311330110603550408130a43616c69666f726e6961311630140603550407130d4d6f756e7461696e20566965773110300e060355040a1307416e64726f69643110300e060355040b1307416e64726f69643110300e06035504031307416e64726f69643122302006092a864886f70d0109011613616e64726f696440616e64726f69642e636f6d30820120300d06092a864886f70d01010105000382010d00308201080282010100d6931904dec60b24b1edc762e0d9d8253e3ecd6ceb1de2ff068ca8e8bca8cd6bd3786ea70aa76ce60ebb0f993559ffd93e77a943e7e83d4b64b8e4fea2d3e656f1e267a81bbfb230b578c20443be4c7218b846f5211586f038a14e89c2be387f8ebecf8fcac3da1ee330c9ea93d0a7c3dc4af350220d50080732e0809717ee6a053359e6a694ec2cb3f284a0a466c87a94d83b31093a67372e2f6412c06e6d42f15818dffe0381cc0cd444da6cddc3b82458194801b32564134fbfde98c9287748dbf5676a540d8154c8bbca07b9e247553311c46b9af76fdeeccc8e69e7c8a2d08e782620943f99727d3c04fe72991d99df9bae38a0b2177fa31d5b6afee91f020103a381fc3081f9301d0603551d0e04160414485900563d272c46ae118605a47419ac09ca8c113081c90603551d230481c13081be8014485900563d272c46ae118605a47419ac09ca8c11a1819aa48197308194310b3009060355040613025553311330110603550408130a43616c69666f726e6961311630140603550407130d4d6f756e7461696e20566965773110300e060355040a1307416e64726f69643110300e060355040b1307416e64726f69643110300e06035504031307416e64726f69643122302006092a864886f70d0109011613616e64726f696440616e64726f69642e636f6d820900936eacbe07f201df300c0603551d13040530030101ff300d06092a864886f70d010105050003820101007aaf968ceb50c441055118d0daabaf015b8a765a27a715a2c2b44f221415ffdace03095abfa42df70708726c2069e5c36eddae0400be29452c084bc27eb6a17eac9dbe182c204eb15311f455d824b656dbe4dc2240912d7586fe88951d01a8feb5ae5a4260535df83431052422468c36e22c2a5ef994d61dd7306ae4c9f6951ba3c12f1d1914ddc61f1a62da2df827f603fea5603b2c540dbd7c019c36bab29a4271c117df523cdbc5f3817a49e0efa60cbd7f74177e7a4f193d43f4220772666e4c4d83e1bd5a86087cf34f2dec21e245ca6c2bb016e683638050d2c430eea7c26a1c49d3760a58ab7f1a82cc938b4831384324bd0401fa12163a50570e684d";
            
            String currentSignature = getPackageManager()
                .getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES)
                .signatures[0].toCharsString();
            
            return originalSignature.equals(currentSignature);
        } catch (Exception e) {
            return false;
        }
    }
}
