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

public class LessondetailActivity extends Activity {
	
	private LessondetailBinding binding;
	
	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		binding = LessondetailBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());
		initialize(_savedInstanceState);
		initializeLogic();
	}
	
	private void initialize(Bundle _savedInstanceState) {
	}
	
	private void initializeLogic() {
		initViews();
		
		dataManager = new DataManager(this);
		lessonId = getIntent().getStringExtra("lesson_id");
		
		setupBackButton();
		loadLessonContent();
		
		
	}
	
	public void _t() {
	} 
	private TextView lessonTitle;
	private ImageButton btnBack;
	private LinearLayout contentContainer;
	private DataManager dataManager;
	private String lessonId;
	
	
	
	private void initViews() {
		lessonTitle = findViewById(R.id.lesson_title);
		btnBack = findViewById(R.id.btn_back);
		contentContainer = findViewById(R.id.lesson_content_container);
	}
	
	private void setupBackButton() {
		btnBack.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}
	
	private void loadLessonContent() {
		ArrayList<HashMap<String, Object>> allLessons = dataManager.getAllLessonsWithSections();
		HashMap<String, Object> currentLesson = null;
		for (HashMap<String, Object> lesson : allLessons) {
			if (lesson.get("id").equals(lessonId)) {
				currentLesson = lesson;
				break;
			}
		}
		if (currentLesson != null) {
			lessonTitle.setText((String) currentLesson.get("title_ar"));
			displaySections((ArrayList<HashMap<String, String>>) currentLesson.get("sections"));
		} else {
			Toast.makeText(this, "لم يتم العثور على الدرس", Toast.LENGTH_SHORT).show();
			finish();
		}
	}
	
	private void displaySections(ArrayList<HashMap<String, String>> sections) {
		if (sections == null || sections.isEmpty()) {
			TextView emptyText = new TextView(this);
			emptyText.setText("لا توجد أقسام في هذا الدرس حالياً");
			emptyText.setTextSize(14);
			emptyText.setTextColor(0xFF7F8C8D);
			emptyText.setPadding(16, 16, 16, 16);
			contentContainer.addView(emptyText);
			return;
		}
		
		for (final HashMap<String, String> section : sections) {
			String type = section.get("type");
			String title = section.get("title");
			final String content = section.get("content");
			String language = section.get("language");
			
			LinearLayout card = new LinearLayout(this);
			card.setOrientation(LinearLayout.VERTICAL);
			card.setBackgroundResource(R.drawable.grid_item_bg);
			card.setPadding(16, 16, 16, 16);
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
			LinearLayout.LayoutParams.MATCH_PARENT,
			LinearLayout.LayoutParams.WRAP_CONTENT
			);
			params.setMargins(0, 0, 0, 12);
			card.setLayoutParams(params);
			
			if (title != null && !title.isEmpty()) {
				TextView titleText = new TextView(this);
				titleText.setText(title);
				titleText.setTextSize(16);
				titleText.setTypeface(Typeface.DEFAULT_BOLD);
				titleText.setTextColor(0xFF193559);
				titleText.setPadding(0, 0, 0, 8);
				card.addView(titleText);
			}
			
			if ("explanation".equals(type)) {
				TextView contentText = new TextView(this);
				contentText.setText(content);
				contentText.setTextSize(14);
				contentText.setTextColor(0xFF555555);
				contentText.setLineSpacing(4, 1);
				card.addView(contentText);
			} 
			else if ("code".equals(type)) {
				TextView codeText = new TextView(this);
				codeText.setText(content);
				codeText.setTextSize(12);
				codeText.setTypeface(Typeface.MONOSPACE);
				codeText.setTextColor(0xFF1A2C3E);
				codeText.setBackgroundColor(0xFFF0F2F5);
				codeText.setPadding(12, 12, 12, 12);
				card.addView(codeText);
			} 
			else if ("tip".equals(type)) {
				TextView tipText = new TextView(this);
				tipText.setText("💡 " + content);
				tipText.setTextSize(13);
				tipText.setTextColor(0xFFF59E0B);
				tipText.setBackgroundColor(0x22F59E0B);
				tipText.setPadding(12, 12, 12, 12);
				card.addView(tipText);
			} 
			else if ("image".equals(type)) {
				final ImageView imageView = new ImageView(this);
				LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT
				);
				imageView.setLayoutParams(imgParams);
				imageView.setAdjustViewBounds(true);
				imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
				card.addView(imageView);
				
				final TextView dummyText = new TextView(this);
				dummyText.setVisibility(View.GONE);
				ImageLoader.getInstance().loadImage(content, new ImageLoader.ImageLoadListener() {
					@Override
					public void onSuccess(Bitmap bitmap) {
						if (bitmap != null) {
							imageView.setImageBitmap(bitmap);
							imageView.setOnClickListener(new View.OnClickListener() {
								@Override
								public void onClick(View v) {
									showImageZoom(content);
								}
							});
							imageView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
								@Override
								public void onGlobalLayout() {
									imageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
									int w = imageView.getDrawable().getIntrinsicWidth();
									int h = imageView.getDrawable().getIntrinsicHeight();
									if (w > 0 && h > 0) {
										int screenWidth = getResources().getDisplayMetrics().widthPixels - dpToPx(32);
										int newWidth = screenWidth;
										int newHeight = (int) ((float) h / w * newWidth);
										LinearLayout.LayoutParams newParams = new LinearLayout.LayoutParams(newWidth, newHeight);
										newParams.setMargins(0, 8, 0, 0);
										imageView.setLayoutParams(newParams);
									}
								}
							});
						}
					}
					@Override
					public void onFailure() {
						imageView.setImageResource(android.R.drawable.ic_menu_gallery);
					}
				}, dummyText);
			} 
			else if ("link".equals(type)) {
				Button linkButton = new Button(this);
				linkButton.setText("🔗 " + (title.isEmpty() ? "فتح الرابط" : title));
				linkButton.setBackgroundResource(R.drawable.button_secondary);
				linkButton.setTextColor(0xFFFFFFFF);
				linkButton.setTextSize(12);
				linkButton.setPadding(16, 12, 16, 12);
				linkButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(content)));
					}
				});
				card.addView(linkButton);
			}
			
			contentContainer.addView(card);
		}
	}
	
	// دالة تكبير الصورة (مكررة هنا للاستقلالية)
	private void showImageZoom(String imageUrl) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		final View dialogView = getLayoutInflater().inflate(R.layout.dialog_image_zoom, null);
		final ImageView zoomImage = dialogView.findViewById(R.id.zoom_image_view);
		
		final TextView dummyText = new TextView(this);
		dummyText.setVisibility(View.GONE);
		ImageLoader.getInstance().loadImage(imageUrl, new ImageLoader.ImageLoadListener() {
			@Override
			public void onSuccess(Bitmap bitmap) {
				if (bitmap != null) {
					zoomImage.setImageBitmap(bitmap);
					zoomImage.setLayoutParams(new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.MATCH_PARENT,
					LinearLayout.LayoutParams.WRAP_CONTENT
					));
					zoomImage.setAdjustViewBounds(true);
				} else {
					zoomImage.setImageResource(android.R.drawable.ic_menu_gallery);
				}
			}
			
			@Override
			public void onFailure() {
				zoomImage.setImageResource(android.R.drawable.ic_menu_gallery);
			}
		}, dummyText);
		
		builder.setView(dialogView);
		builder.setPositiveButton("إغلاق", null);
		builder.show();
	}
	
	private int dpToPx(int dp) {
		return (int) (dp * getResources().getDisplayMetrics().density);
	}
	
	private void initializeLogic_Dummy() {
	}
	
}