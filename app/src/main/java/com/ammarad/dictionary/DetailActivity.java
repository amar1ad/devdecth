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
import android.content.Intent;
import android.content.ClipboardManager;
import android.view.ViewTreeObserver;

public class DetailActivity extends Activity {
	
	private DetailBinding binding;
	private DataManager dataManager;
	private String termId;
	
	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		binding = DetailBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());
		initialize(_savedInstanceState);
		initializeLogic();
	}
	
	private void initialize(Bundle _savedInstanceState) {
	}
	
	private void initializeLogic() {
		initViews();
		
		dataManager = new DataManager(this);
		termId = getIntent().getStringExtra("term_id");
		
		HashMap<String, Object> termWithExtras = dataManager.getTermWithExtrasById(termId);
		if (termWithExtras != null) {
			termName = (String) termWithExtras.get("term_ar");
			termDefinitionText = (String) termWithExtras.get("definition_ar");
			termExampleText = (String) termWithExtras.get("example_code");
			termLanguageText = (String) termWithExtras.get("language");
			extraFields = (ArrayList<HashMap<String, String>>) termWithExtras.get("extra_fields");
			displayData();
			displayExtraFields();
		} else {
			HashMap<String, String> term = dataManager.getTermById(termId);
			if (term != null) {
				termName = term.get("term_ar");
				termDefinitionText = term.get("definition_ar");
				termExampleText = term.get("example_code");
				termLanguageText = term.get("language");
				extraFields = new ArrayList<>();
				displayData();
			} else {
				Toast.makeText(this, "لم يتم العثور على المصطلح", Toast.LENGTH_SHORT).show();
				finish();
				return;
			}
		}
		setupButtons();
		
	}
	
	public void _h() {
	} 
	private TextView termTitle;
	private TextView termLanguage;
	private TextView termDefinition;
	private TextView termExample;
	private ImageButton btnBack;
	private ImageButton btnCopy;
	private Button btnShare;
	private LinearLayout extraFieldsContainer;
	
	// private DataManager dataManager;
	// private String termId;
	private String termName;
	private String termDefinitionText;
	private String termExampleText;
	private String termLanguageText;
	private ArrayList<HashMap<String, String>> extraFields;
	
	
	
	
	private void initViews() {
		termTitle = findViewById(R.id.term_title);
		termLanguage = findViewById(R.id.term_language);
		termDefinition = findViewById(R.id.term_definition);
		termExample = findViewById(R.id.term_example);
		btnBack = findViewById(R.id.btn_back);
		btnCopy = findViewById(R.id.btn_copy);
		btnShare = findViewById(R.id.btn_share_term);
		extraFieldsContainer = findViewById(R.id.extra_fields_container);
	}
	
	private void displayData() {
		termTitle.setText(termName);
		termDefinition.setText(termDefinitionText);
		termExample.setText(termExampleText);
		if (termLanguageText != null && !termLanguageText.isEmpty()) {
			termLanguage.setText("لغة: " + termLanguageText);
		} else {
			termLanguage.setVisibility(View.GONE);
		}
	}
	
	private void displayExtraFields() {
		if (extraFields == null || extraFields.isEmpty()) {
			extraFieldsContainer.setVisibility(View.GONE);
			return;
		}
		extraFieldsContainer.removeAllViews();
		extraFieldsContainer.setVisibility(View.VISIBLE);
		
		TextView sectionTitle = new TextView(this);
		sectionTitle.setText("📦 محتوى إضافي");
		sectionTitle.setTextSize(16);
		sectionTitle.setTypeface(Typeface.DEFAULT_BOLD);
		sectionTitle.setTextColor(0xFF193559);
		sectionTitle.setPadding(0, 16, 0, 8);
		extraFieldsContainer.addView(sectionTitle);
		
		for (final HashMap<String, String> field : extraFields) {
			String type = field.get("type");
			String title = field.get("title");
			final String content = field.get("content");
			String language = field.get("language");
			
			LinearLayout card = new LinearLayout(this);
			card.setOrientation(LinearLayout.VERTICAL);
			card.setPadding(16, 16, 16, 16);
			card.setBackgroundResource(R.drawable.grid_item_bg);
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
			LinearLayout.LayoutParams.MATCH_PARENT,
			LinearLayout.LayoutParams.WRAP_CONTENT
			);
			params.setMargins(0, 0, 0, 12);
			card.setLayoutParams(params);
			
			if (title != null && !title.isEmpty()) {
				TextView titleText = new TextView(this);
				titleText.setText(title);
				titleText.setTextSize(14);
				titleText.setTypeface(Typeface.DEFAULT_BOLD);
				titleText.setTextColor(0xFF193559);
				titleText.setPadding(0, 0, 0, 8);
				card.addView(titleText);
			}
			
			if ("code".equals(type)) {
				TextView codeText = new TextView(this);
				codeText.setText(content);
				codeText.setTextSize(12);
				codeText.setTypeface(Typeface.MONOSPACE);
				codeText.setTextColor(0xFF1A2C3E);
				codeText.setBackgroundColor(0xFFF0F2F5);
				codeText.setPadding(12, 12, 12, 12);
				card.addView(codeText);
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
							// إضافة حدث النقر لتكبير الصورة
							imageView.setOnClickListener(new View.OnClickListener() {
								@Override
								public void onClick(View v) {
									showImageZoom(content);
								}
							});
							// تحسين الأبعاد بعد التحميل
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
			else {
				TextView contentText = new TextView(this);
				contentText.setText(content);
				contentText.setTextSize(13);
				contentText.setTextColor(0xFF555555);
				contentText.setLineSpacing(4, 1);
				card.addView(contentText);
			}
			extraFieldsContainer.addView(card);
		}
	}
	
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
	
	private void setupButtons() {
		btnBack.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		btnCopy.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				copyToClipboard();
			}
		});
		btnShare.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				shareTerm();
			}
		});
	}
	
	private void copyToClipboard() {
		String code = termExampleText;
		android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
		android.content.ClipData clip = android.content.ClipData.newPlainText("code", code);
		clipboard.setPrimaryClip(clip);
		Toast.makeText(this, "📋 تم نسخ الكود", Toast.LENGTH_SHORT).show();
	}
	
	private void shareTerm() {
		Intent shareIntent = new Intent(Intent.ACTION_SEND);
		shareIntent.setType("text/plain");
		String shareText = "📚 " + termName + "\n\n📖 " + termDefinitionText + "\n\n💻 مثال:\n" + termExampleText;
		shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
		startActivity(Intent.createChooser(shareIntent, "مشاركة المصطلح"));
	}
	
	private void initializeLogic_Dummy() {
	}
	
}