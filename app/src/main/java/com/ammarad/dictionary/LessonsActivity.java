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

public class LessonsActivity extends Activity {
	
	private LessonsBinding binding;
	
	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		binding = LessonsBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());
		initialize(_savedInstanceState);
		initializeLogic();
	}
	
	private void initialize(Bundle _savedInstanceState) {
	}
	
	private void initializeLogic() {
		languageSlug = getIntent().getStringExtra("language_slug");
		languageName = getIntent().getStringExtra("language_name");
		
		dataManager = new DataManager(this);
		
		initViews();
		setupBackButton();
		loadLessons();
		
		
	}
	
	public void _t() {
	} 
	private ListView listView;
	private ProgressBar progressBar;
	private TextView languageTitle;
	private DataManager dataManager;
	private ArrayList<HashMap<String, Object>> lessons;
	private String languageSlug;
	private String languageName;
	
	
	private void initViews() {
		listView = findViewById(R.id.list_lessons);
		progressBar = findViewById(R.id.progressBar);
		languageTitle = findViewById(R.id.language_title);
		languageTitle.setText("دروس " + languageName);
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
	
	private void loadLessons() {
		progressBar.setVisibility(ProgressBar.VISIBLE);
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(300); // محاكاة تحميل بسيطة (يمكن إلغاؤها)
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							lessons = dataManager.getLessonsByLanguageWithSections(languageSlug);
							
							if (lessons == null || lessons.isEmpty()) {
								progressBar.setVisibility(ProgressBar.GONE);
								Toast.makeText(LessonsActivity.this, "لا توجد دروس متاحة حالياً", Toast.LENGTH_SHORT).show();
								return;
							}
							
							LessonsAdapter adapter = new LessonsAdapter(LessonsActivity.this, lessons);
							listView.setAdapter(adapter);
							progressBar.setVisibility(ProgressBar.GONE);
						}
					});
				} catch (Exception e) {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							progressBar.setVisibility(ProgressBar.GONE);
							Toast.makeText(LessonsActivity.this, "فشل تحميل الدروس", Toast.LENGTH_SHORT).show();
						}
					});
				}
			}
		}).start();
	}
	
	// كلاس الـ Adapter للدروس
	class LessonsAdapter extends BaseAdapter {
		
		private Context context;
		private ArrayList<HashMap<String, Object>> data;
		
		public LessonsAdapter(Context context, ArrayList<HashMap<String, Object>> data) {
			this.context = context;
			this.data = data;
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
				convertView = getLayoutInflater().inflate(R.layout.lesson_item, parent, false);
			}
			
			TextView title = convertView.findViewById(R.id.lesson_title);
			TextView description = convertView.findViewById(R.id.lesson_description);
			TextView order = convertView.findViewById(R.id.lesson_order);
			
			final HashMap<String, Object> lesson = data.get(position);
			
			title.setText((String) lesson.get("title_ar"));
			description.setText((String) lesson.get("description_ar"));
			order.setText(String.valueOf(lesson.get("order")));
			
			convertView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(context, LessondetailActivity.class);
					intent.putExtra("lesson_id", (String) lesson.get("id"));
					context.startActivity(intent);
				}
			});
			
			return convertView;
		}
	}
	
	private void initializeLogic_Dummy() {
	}
	
}