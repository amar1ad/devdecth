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
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import com.ammarad.dictionary.databinding.*;
import java.io.*;
import java.text.*;
import java.util.*;
import java.util.regex.*;
import org.json.*;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.HashMap;

public class TermsActivity extends Activity {
	
	private TermsBinding binding;
	private FavoritesManager favoritesManager;
	private DataManager dataManager;
	
	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		binding = TermsBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());
		initialize(_savedInstanceState);
		initializeLogic();
	}
	
	private void initialize(Bundle _savedInstanceState) {
	}
	
	private void initializeLogic() {
		categoryId = getIntent().getStringExtra("category_id");
		categoryName = getIntent().getStringExtra("category_name");
		
		dataManager = new DataManager(this);
		favoritesManager = new FavoritesManager(this);
		
		initViews();
		loadTerms();
		setupBackButton();
		setupSearch();
		
	}
	
	public void _y() {
	} 
	private String categoryId;
	private String categoryName;
	private ListView listView;
	private SearchView searchView;
	private ProgressBar progressBar;
	private TextView categoryTitle;
	private TextView termsCount;
	
	// private DataManager dataManager;
	private ArrayList<HashMap<String, Object>> terms;  // تغيير إلى Object ليشمل extra_fields
	private ArrayList<HashMap<String, Object>> filteredTerms;
	private TermsAdapter adapter;
	// private FavoritesManager favoritesManager;
	private void initViews() {
		listView = findViewById(R.id.list_terms);
		searchView = findViewById(R.id.search_terms);
		progressBar = findViewById(R.id.progressBar);
		categoryTitle = findViewById(R.id.category_title);
		termsCount = findViewById(R.id.terms_count);
		
		categoryTitle.setText(categoryName);
		terms = new ArrayList<>();
		filteredTerms = new ArrayList<>();
	}
	
	private void loadTerms() {
		progressBar.setVisibility(ProgressBar.VISIBLE);
		
		// استخدام getTermsByCategoryWithExtras (سنضيفها لاحقاً)
		// حالياً نستخدم الطريقة المؤقتة
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					// محاكاة تحميل البيانات (سيتم جلبها من DataManager)
					Thread.sleep(500);
					
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							// للحصول على المصطلحات مع الحقول الإضافية
							// dataManager.getTermsByCategoryWithExtras(categoryId);
							// مؤقتاً نستخدم getTermsByCategory العادية
							ArrayList<HashMap<String, String>> basicTerms = dataManager.getTermsByCategory(categoryId);
							terms.clear();
							for (HashMap<String, String> basic : basicTerms) {
								HashMap<String, Object> term = new HashMap<>();
								term.put("id", basic.get("id"));
								term.put("term_ar", basic.get("term_ar"));
								term.put("term_en", basic.get("term_en"));
								term.put("definition_ar", basic.get("definition_ar"));
								term.put("definition_en", basic.get("definition_en"));
								term.put("example_code", basic.get("example_code"));
								term.put("category_id", basic.get("category_id"));
								term.put("language", basic.get("language"));
								term.put("extra_fields", new ArrayList<HashMap<String, String>>());
								terms.add(term);
							}
							
							filteredTerms.clear();
							filteredTerms.addAll(terms);
							
							adapter = new TermsAdapter(TermsActivity.this, filteredTerms);
							listView.setAdapter(adapter);
							termsCount.setText(filteredTerms.size() + " مصطلح");
							progressBar.setVisibility(ProgressBar.GONE);
						}
					});
					
				} catch (Exception e) {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							progressBar.setVisibility(ProgressBar.GONE);
							Toast.makeText(TermsActivity.this, "فشل تحميل المصطلحات", Toast.LENGTH_SHORT).show();
						}
					});
				}
			}
		}).start();
	}
	
	private void setupBackButton() {
		ImageButton btnBack = findViewById(R.id.btn_back);
		btnBack.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
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
				filterTerms(newText);
				return true;
			}
		});
	}
	
	private void filterTerms(String query) {
		final String searchQuery = query.trim().toLowerCase();
		filteredTerms.clear();
		
		if (searchQuery.isEmpty()) {
			filteredTerms.addAll(terms);
		} else {
			for (HashMap<String, Object> term : terms) {
				String termAr = (String) term.get("term_ar");
				String termEn = (String) term.get("term_en");
				if ((termAr != null && termAr.toLowerCase().contains(searchQuery)) ||
				(termEn != null && termEn.toLowerCase().contains(searchQuery))) {
					filteredTerms.add(term);
				}
			}
		}
		
		if (adapter != null) {
			adapter.updateData(filteredTerms);
			termsCount.setText(filteredTerms.size() + " مصطلح");
		}
	}
	
	// كلاس الـ Adapter للمصطلحات
	class TermsAdapter extends BaseAdapter {
		
		private Context context;
		private ArrayList<HashMap<String, Object>> data;
		private FavoritesManager favManager;
		
		public TermsAdapter(Context context, ArrayList<HashMap<String, Object>> data) {
			this.context = context;
			this.data = data;
			this.favManager = new FavoritesManager(context);
		}
		
		public void updateData(ArrayList<HashMap<String, Object>> newData) {
			this.data = newData;
			notifyDataSetChanged();
		}
		
		@Override
		public int getCount() {
			return data.size();
		}
		
		@Override
		public Object getItem(int position) {
			return data.get(position);
		}
		
		@Override
		public long getItemId(int position) {
			return position;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = getLayoutInflater().inflate(R.layout.term_item, parent, false);
			}
			
			TextView titleAr = convertView.findViewById(R.id.term_title_ar);
			TextView titleEn = convertView.findViewById(R.id.term_title_en);
			TextView definition = convertView.findViewById(R.id.term_definition);
			final ImageButton btnFavorite = convertView.findViewById(R.id.btn_favorite);
			final ImageView iconImage = convertView.findViewById(R.id.term_icon_image);
			final TextView iconText = convertView.findViewById(R.id.term_icon_text);
			
			final HashMap<String, Object> term = data.get(position);
			final String termId = (String) term.get("id");
			final String termName = (String) term.get("term_ar");
			final String termDefinition = (String) term.get("definition_ar");
			final String termExample = (String) term.get("example_code");
			final String termLanguage = (String) term.get("language");
			
			titleAr.setText((String) term.get("term_ar"));
			titleEn.setText((String) term.get("term_en"));
			definition.setText((String) term.get("definition_ar"));
			
			// أيقونة افتراضية للمصطلحات
			iconText.setText("");
			iconText.setVisibility(View.VISIBLE);
			iconImage.setVisibility(View.GONE);
			
			// تحديث حالة النجمة
			if (favManager.isFavorite(termId)) {
				btnFavorite.setImageResource(android.R.drawable.btn_star_big_on);
			} else {
				btnFavorite.setImageResource(android.R.drawable.btn_star_big_off);
			}
			
			// إضافة/إزالة من المفضلة
			btnFavorite.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (favManager.isFavorite(termId)) {
						favManager.removeFromFavorites(termId);
						btnFavorite.setImageResource(android.R.drawable.btn_star_big_off);
						Toast.makeText(context, "تم الإزالة من المفضلة", Toast.LENGTH_SHORT).show();
					} else {
						favManager.addToFavorites(termId, termName, termDefinition, termExample, termLanguage);
						btnFavorite.setImageResource(android.R.drawable.btn_star_big_on);
						Toast.makeText(context, "تمت الإضافة إلى المفضلة", Toast.LENGTH_SHORT).show();
					}
				}
			});
			
			// فتح التفاصيل عند الضغط على العنصر
			convertView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(context, DetailActivity.class);
					intent.putExtra("term_id", termId);
					context.startActivity(intent);
				}
			});
			
			return convertView;
		}
	}
	
	private void initializeLogic_Dummy() {
	}
	
}