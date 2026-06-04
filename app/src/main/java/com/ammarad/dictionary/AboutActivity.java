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

public class AboutActivity extends Activity {
	
	private AboutBinding binding;
	
	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		binding = AboutBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());
		initialize(_savedInstanceState);
		initializeLogic();
	}
	
	private void initialize(Bundle _savedInstanceState) {
	}
	
	private void initializeLogic() {
		dataManager = new DataManager(this);
		
		initViews();
		setupSocialButtons();
		setupBackButton();
		updateStats();
	}
	
	public void _y() {
	} 
	private DataManager dataManager;
	private TextView statsCategories;
	private TextView statsTerms;
	private void initViews() {
		statsCategories = findViewById(R.id.stats_categories_about);
		statsTerms = findViewById(R.id.stats_terms_about);
	}
	
	private void updateStats() {
		statsCategories.setText(String.valueOf(dataManager.getCategories().size()));
		statsTerms.setText(String.valueOf(dataManager.getTotalTerms()));
	}
	
	private void setupSocialButtons() {
		ImageButton btnX = findViewById(R.id.social_x);
		ImageButton btnInstagram = findViewById(R.id.social_instagram);
		ImageButton btnFacebook = findViewById(R.id.social_facebook);
		ImageButton btnTelegram = findViewById(R.id.social_telegram);
		ImageButton btnTikTok = findViewById(R.id.social_tiktok);
		
		btnX.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				openUrl("https://x.com/ammar_12ad1");
			}
		});
		
		btnInstagram.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				openUrl("https://www.instagram.com/ammar_12ad");
			}
		});
		
		btnFacebook.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				openUrl("https://www.facebook.com/amar12ad1");
			}
		});
		
		btnTelegram.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				openUrl("https://t.me/progmamar");
			}
		});
		
		btnTikTok.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				openUrl("https://tiktok.com/@ammar_12ad");
			}
		});
	}
	
	private void setupBackButton() {
		Button btnBack = findViewById(R.id.btn_back);
		btnBack.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}
	
	private void openUrl(String url) {
		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		startActivity(browserIntent);
	}
	
	private void initializeLogic_Dummy() {
	}
	
}