package com.ammarad.dictionary;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;

public class FavoritesManager {
    
    private static final String PREFS_NAME = "favorites_prefs";
    private static final String KEY_FAVORITES = "favorites_list";
    
    private SharedPreferences prefs;
    
    public FavoritesManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    // إضافة مصطلح إلى المفضلة
    public void addToFavorites(String termId, String termName, String definition, String exampleCode, String language) {
        try {
            JSONArray favoritesArray = getFavoritesArray();
            
            // التحقق إذا كان موجوداً بالفعل
            for (int i = 0; i < favoritesArray.length(); i++) {
                JSONObject obj = favoritesArray.getJSONObject(i);
                if (obj.getString("id").equals(termId)) {
                    return; // موجود بالفعل
                }
            }
            
            // إضافة مصطلح جديد
            JSONObject termObj = new JSONObject();
            termObj.put("id", termId);
            termObj.put("name", termName);
            termObj.put("definition", definition);
            termObj.put("example", exampleCode);
            termObj.put("language", language);
            favoritesArray.put(termObj);
            
            prefs.edit().putString(KEY_FAVORITES, favoritesArray.toString()).apply();
        } catch (Exception e) {}
    }
    
    // إزالة مصطلح من المفضلة
    public void removeFromFavorites(String termId) {
        try {
            JSONArray favoritesArray = getFavoritesArray();
            JSONArray newArray = new JSONArray();
            
            for (int i = 0; i < favoritesArray.length(); i++) {
                JSONObject obj = favoritesArray.getJSONObject(i);
                if (!obj.getString("id").equals(termId)) {
                    newArray.put(obj);
                }
            }
            
            prefs.edit().putString(KEY_FAVORITES, newArray.toString()).apply();
        } catch (Exception e) {}
    }
    
    // التحقق إذا كان المصطلح مفضلاً
    public boolean isFavorite(String termId) {
        try {
            JSONArray favoritesArray = getFavoritesArray();
            for (int i = 0; i < favoritesArray.length(); i++) {
                JSONObject obj = favoritesArray.getJSONObject(i);
                if (obj.getString("id").equals(termId)) {
                    return true;
                }
            }
        } catch (Exception e) {}
        return false;
    }
    
    // جلب قائمة المفضلة
    public ArrayList<HashMap<String, String>> getFavorites() {
        ArrayList<HashMap<String, String>> favorites = new ArrayList<>();
        try {
            JSONArray favoritesArray = getFavoritesArray();
            for (int i = 0; i < favoritesArray.length(); i++) {
                JSONObject obj = favoritesArray.getJSONObject(i);
                HashMap<String, String> term = new HashMap<>();
                term.put("id", obj.getString("id"));
                term.put("name", obj.getString("name"));
                term.put("definition", obj.getString("definition"));
                term.put("example", obj.getString("example"));
                term.put("language", obj.optString("language", "General"));
                favorites.add(term);
            }
        } catch (Exception e) {}
        return favorites;
    }
    
    // الحصول على JSONArray من المفضلة
    private JSONArray getFavoritesArray() {
        String saved = prefs.getString(KEY_FAVORITES, "[]");
        try {
            return new JSONArray(saved);
        } catch (Exception e) {
            return new JSONArray();
        }
    }
}