package com.ammarad.dictionary;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.HashMap;
import android.content.Intent;

public class GridAdapter extends BaseAdapter {

    private ArrayList<HashMap<String, String>> originalData;
    private ArrayList<HashMap<String, String>> filteredData;
    private boolean isArabic;
    private MainActivity activity;

    public GridAdapter(MainActivity activity, ArrayList<HashMap<String, String>> data, boolean isArabic) {
        this.activity = activity;
        this.originalData = new ArrayList<>(data);
        this.filteredData = new ArrayList<>(data);
        this.isArabic = isArabic;
    }

    public void updateData(ArrayList<HashMap<String, String>> newData) {
        this.originalData = new ArrayList<>(newData);
        this.filteredData = new ArrayList<>(newData);
        notifyDataSetChanged();
    }

    public void setLanguage(boolean isArabic) {
        this.isArabic = isArabic;
        notifyDataSetChanged();
    }

    public void filter(String query) {
        String searchQuery = query.trim().toLowerCase();
        filteredData.clear();

        if (searchQuery.isEmpty()) {
            filteredData.addAll(originalData);
        } else {
            for (HashMap<String, String> cat : originalData) {
                String nameAr = cat.get("name_ar");
                String nameEn = cat.get("name_en");
                if ((nameAr != null && nameAr.toLowerCase().contains(searchQuery)) ||
                    (nameEn != null && nameEn.toLowerCase().contains(searchQuery))) {
                    filteredData.add(cat);
                }
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return filteredData.size();
    }

    @Override
    public Object getItem(int position) {
        return filteredData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = activity.getLayoutInflater().inflate(R.layout.grid_item, parent, false);
        }

        final ImageView iconImage = convertView.findViewById(R.id.grid_item_icon_image);
        final TextView iconText = convertView.findViewById(R.id.grid_item_icon_text);
        TextView titleAr = convertView.findViewById(R.id.grid_item_title_ar);
        TextView titleEn = convertView.findViewById(R.id.grid_item_title_en);
        TextView count = convertView.findViewById(R.id.grid_item_count);

        final HashMap<String, String> item = filteredData.get(position);

        if (isArabic) {
            titleAr.setVisibility(View.VISIBLE);
            titleEn.setVisibility(View.GONE);
            titleAr.setText(item.get("name_ar"));
        } else {
            titleAr.setVisibility(View.GONE);
            titleEn.setVisibility(View.VISIBLE);
            titleEn.setText(item.get("name_en"));
        }

        String countStr = item.get("count");
        count.setText((countStr != null ? countStr : "0") + " مصطلح");

        final String iconStr = item.get("icon");
        ImageLoader.getInstance().loadImage(iconStr, iconImage, iconText);

        final String selectedId = item.get("id");
        final String selectedName = isArabic ? item.get("name_ar") : item.get("name_en");

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, TermsActivity.class);
                intent.putExtra("category_id", selectedId);
                intent.putExtra("category_name", selectedName);
                activity.startActivity(intent);
            }
        });

        return convertView;
    }
}