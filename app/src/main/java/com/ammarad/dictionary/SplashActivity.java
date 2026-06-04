package com.ammarad.dictionary;

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

public class SplashActivity extends Activity {
	
	private SplashBinding binding;
	
	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		binding = SplashBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());
		initialize(_savedInstanceState);
		initializeLogic();
	}
	
	private void initialize(Bundle _savedInstanceState) {
	}
	
	private void initializeLogic() {
		progressBar = findViewById(R.id.progressBar);
		loadingText = findViewById(R.id.loading_text);
		
		dataManager = new DataManager(this);
		handler = new Handler();
		
		// بدء تحميل البيانات
		loadData();
	}
	
	public void _t() {
	} 
	private ProgressBar progressBar;
	private TextView loadingText;
	private DataManager dataManager;
	private Handler handler;
	private void loadData() {
		loadingText.setText("جاري تحميل البيانات...");
		
		if (dataManager.hasLocalData()) {
			// توجد بيانات مخزنة محلياً
			loadingText.setText("بيانات مخزنة محلياً");
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					goToMainActivity();
				}
			}, 1500);
		} else {
			// أول تشغيل - جلب البيانات من الخادم
			loadingText.setText("جاري التحميل من الخادم...");
			dataManager.fetchAllData(new DataManager.DataCallback() {
				@Override
				public void onSuccess() {
					loadingText.setText("تم التحميل بنجاح");
					handler.postDelayed(new Runnable() {
						@Override
						public void run() {
							goToMainActivity();
						}
					}, 1000);
				}
				
				@Override
				public void onFailure(String error) {
					loadingText.setText("خطأ: " + error);
					handler.postDelayed(new Runnable() {
						@Override
						public void run() {
							goToMainActivity();
						}
					}, 2000);
				}
			});
		}
	}
	
	private void goToMainActivity() {
		Intent intent = new Intent(SplashActivity.this, MainActivity.class);
		startActivity(intent);
		overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
		finish();
	}
	
	private void initializeLogic_Dummy() {
	}
	
}