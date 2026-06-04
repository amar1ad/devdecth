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

public class FavoritesActivity extends Activity {
	
	private FavoritesBinding binding;
	
	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		binding = FavoritesBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());
		initialize(_savedInstanceState);
		initializeLogic();
	}
	
	private void initialize(Bundle _savedInstanceState) {
	}
	
	private void initializeLogic() {
		initViews();
		setupBackButton();
		loadFavorites();
		setupClearAllButton();
	}
	
	public void _t() {
	} 
	private ListView listView;
	private TextView emptyView;
	private TextView favCount;
	private FavoritesManager favoritesManager;
	private ArrayList<HashMap<String, String>> favorites;
	private FavoritesAdapter adapter;
	private void initViews() {
		listView = findViewById(R.id.list_favorites);
		emptyView = findViewById(R.id.empty_view);
		favCount = findViewById(R.id.fav_count);
		favoritesManager = new FavoritesManager(this);
		favorites = new ArrayList<>();
	}
	
	private void loadFavorites() {
		favorites = favoritesManager.getFavorites();
		
		if (favorites.isEmpty()) {
			emptyView.setVisibility(View.VISIBLE);
			listView.setVisibility(View.GONE);
			favCount.setText("0 مصطلح مفضل");
		} else {
			emptyView.setVisibility(View.GONE);
			listView.setVisibility(View.VISIBLE);
			adapter = new FavoritesAdapter(this, favorites);
			listView.setAdapter(adapter);
			favCount.setText(favorites.size() + " مصطلح مفضل");
		}
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
	private void setupClearAllButton() {
		Button btnClearAll = findViewById(R.id.btn_clear_all);
		btnClearAll.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				new android.app.AlertDialog.Builder(FavoritesActivity.this)
				.setTitle("مسح الكل")
				.setMessage("هل تريد حذف جميع المصطلحات من المفضلة؟")
				.setPositiveButton("نعم", new android.content.DialogInterface.OnClickListener() {
					@Override
					public void onClick(android.content.DialogInterface dialog, int which) {
						for (HashMap<String, String> term : favorites) {
							favoritesManager.removeFromFavorites(term.get("id"));
						}
						loadFavorites();
						Toast.makeText(FavoritesActivity.this, "🗑️ تم مسح جميع المفضلة", Toast.LENGTH_SHORT).show();
					}
				})
				.setNegativeButton("لا", null)
				.show();
			}
		});
	}
	// كلاس الـ Adapter المحسن
	
	class FavoritesAdapter extends BaseAdapter {
		
		private Context context;
		private ArrayList<HashMap<String, String>> data;
		
		public FavoritesAdapter(Context context, ArrayList<HashMap<String, String>> data) {
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
				convertView = getLayoutInflater().inflate(R.layout.favorite_item, parent, false);
			}
			
			TextView termName = convertView.findViewById(R.id.fav_term_name);
			TextView termDefinition = convertView.findViewById(R.id.fav_term_definition);
			ImageButton btnDelete = convertView.findViewById(R.id.btn_delete_favorite);
			
			final HashMap<String, String> term = data.get(position);
			
			termName.setText(term.get("name"));
			termDefinition.setText(term.get("definition"));
			
			// فتح التفاصيل عند الضغط على العنصر
			convertView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(context, DetailActivity.class);
					intent.putExtra("term_id", term.get("id"));
					context.startActivity(intent);
				}
			});
			
			// حذف من المفضلة عند الضغط على زر الحذف
			btnDelete.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					favoritesManager.removeFromFavorites(term.get("id"));
					loadFavorites();
					Toast.makeText(context, "🗑️ تم الإزالة من المفضلة", Toast.LENGTH_SHORT).show();
				}
			});
			
			// حذف بالضغط المطول
			convertView.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					showDeleteDialog(term.get("id"), term.get("name"));
					return true;
				}
			});
			
			return convertView;
		}
		
		private void showDeleteDialog(final String termId, final String termName) {
			new android.app.AlertDialog.Builder(context)
			.setTitle("حذف من المفضلة")
			.setMessage("هل تريد إزالة \"" + termName + "\" من المفضلة؟")
			.setPositiveButton("نعم", new android.content.DialogInterface.OnClickListener() {
				@Override
				public void onClick(android.content.DialogInterface dialog, int which) {
					favoritesManager.removeFromFavorites(termId);
					loadFavorites();
					Toast.makeText(context, "🗑️ تم إزالة \"" + termName + "\" من المفضلة", Toast.LENGTH_SHORT).show();
				}
			})
			.setNegativeButton("لا", null)
			.show();
		}
	}
	
	private void initializeLogic_Dummy() {
	}
	
}