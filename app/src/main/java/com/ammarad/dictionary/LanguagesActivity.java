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

public class LanguagesActivity extends Activity {
	
	private LanguagesBinding binding;
	
	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		binding = LanguagesBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());
		initialize(_savedInstanceState);
		initializeLogic();
	}
	
	private void initialize(Bundle _savedInstanceState) {
	}
	
	private void initializeLogic() {
		dataManager = new DataManager(this);
		languages = dataManager.getLanguages();
		
		setupBackButton();
		setupListView();
		
	}
	
	public void _t() {
	} 
	private ListView listView;
	private DataManager dataManager;
	private ArrayList<HashMap<String, String>> languages;
	private void setupBackButton() {
		ImageButton btnBack = findViewById(R.id.btn_back);
		btnBack.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}
	
	private void setupListView() {
		listView = findViewById(R.id.list_languages);
		LanguagesAdapter adapter = new LanguagesAdapter(this, languages);
		listView.setAdapter(adapter);
	}
	
	class LanguagesAdapter extends BaseAdapter {
		
		private Context context;
		private ArrayList<HashMap<String, String>> data;
		
		public LanguagesAdapter(Context context, ArrayList<HashMap<String, String>> data) {
			this.context = context;
			this.data = data;
		}
		
		@Override
		public int getCount() { return data.size(); }
		
		@Override
		public Object getItem(int p) { return data.get(p); }
		
		@Override
		public long getItemId(int p) { return p; }
		
		@Override
		public View getView(int p, View v, ViewGroup g) {
			if (v == null) {
				v = getLayoutInflater().inflate(R.layout.language_item, g, false);
			}
			
			ImageView iconImage = v.findViewById(R.id.language_icon_image);
			TextView iconText = v.findViewById(R.id.language_icon_text);
			TextView nameAr = v.findViewById(R.id.language_name_ar);
			TextView nameEn = v.findViewById(R.id.language_name_en);
			
			final HashMap<String, String> item = data.get(p);
			
			nameAr.setText(item.get("name_ar"));
			nameEn.setText(item.get("name_en"));
			
			String iconStr = item.get("icon");
			ImageLoader.getInstance().loadImage(iconStr, iconImage, iconText);
			
			v.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					Intent intent = new Intent(context, LessonsActivity.class);
					intent.putExtra("language_slug", item.get("slug"));
					intent.putExtra("language_name", item.get("name_ar"));
					context.startActivity(intent);
				}
			});
			
			return v;
		}
	}
	
	private void initializeLogic_Dummy() {
	}
	
}