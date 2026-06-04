package com.ammarad.dictionary;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class DataManager {
    
    private Context context;
    private SharedPreferences prefs;
    
    private static final String PREFS_NAME = "dictionary_data";
    private static final String KEY_CATEGORIES = "categories";
    private static final String KEY_TERMS = "terms";
    private static final String KEY_LANGUAGES = "languages";
    private static final String KEY_LESSONS = "lessons";
    private static final String KEY_LAST_UPDATE = "last_update";
    private static final String KEY_DATA_VERSION = "data_version";
    
    // القوائم الأساسية
    private ArrayList<HashMap<String, String>> categories;
    private ArrayList<HashMap<String, String>> allTerms;
    private ArrayList<HashMap<String, String>> languages;
    private ArrayList<HashMap<String, String>> lessons;
    
    // القوائم الموسعة (مع الحقول الإضافية والأقسام)
    private ArrayList<HashMap<String, Object>> allTermsWithExtras;
    private ArrayList<HashMap<String, Object>> allLessonsWithSections;
    
    public interface DataCallback {
        void onSuccess();
        void onFailure(String error);
    }
    
    public DataManager(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.categories = new ArrayList<>();
        this.allTerms = new ArrayList<>();
        this.languages = new ArrayList<>();
        this.lessons = new ArrayList<>();
        this.allTermsWithExtras = new ArrayList<>();
        this.allLessonsWithSections = new ArrayList<>();
        loadFromLocal();
    }
    
    // جلب جميع البيانات من الخادم
    public void fetchAllData(final DataCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL("https://raw.githubusercontent.com/amar1ad/My_app/refs/heads/main/data.json");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(15000);
                    
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    reader.close();
                    connection.disconnect();
                    
                    final String jsonData = result.toString();
                    JSONObject fullObject = new JSONObject(jsonData);
                    
                    saveAllData(fullObject);
                    loadFromLocal();
                    
                    ((android.app.Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.onSuccess();
                        }
                    });
                    
                } catch (Exception e) {
                    ((android.app.Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.onFailure("فشل الاتصال بالإنترنت");
                        }
                    });
                }
            }
        }).start();
    }
    
    // تحديث البيانات
    public void refreshData(final DataCallback callback) {
        fetchAllData(callback);
    }
    
    private void saveAllData(JSONObject fullObject) {
        try {
            SharedPreferences.Editor editor = prefs.edit();
            
            JSONArray categoriesArray = fullObject.getJSONArray("categories");
            editor.putString(KEY_CATEGORIES, categoriesArray.toString());
            
            JSONArray termsArray = fullObject.getJSONArray("terms");
            editor.putString(KEY_TERMS, termsArray.toString());
            
            JSONArray languagesArray = fullObject.getJSONArray("languages");
            editor.putString(KEY_LANGUAGES, languagesArray.toString());
            
            JSONArray lessonsArray = fullObject.getJSONArray("lessons");
            editor.putString(KEY_LESSONS, lessonsArray.toString());
            
            editor.putString(KEY_DATA_VERSION, fullObject.optString("version", "1.0.0"));
            editor.putLong(KEY_LAST_UPDATE, System.currentTimeMillis());
            editor.apply();
            
        } catch (Exception e) {}
    }
    
    private void loadFromLocal() {
        categories.clear();
        allTerms.clear();
        languages.clear();
        lessons.clear();
        allTermsWithExtras.clear();
        allLessonsWithSections.clear();
        
        // ==================== تحميل التصنيفات ====================
        String categoriesJson = prefs.getString(KEY_CATEGORIES, "");
        if (!categoriesJson.isEmpty()) {
            try {
                JSONArray array = new JSONArray(categoriesJson);
                for (int i = 0; i < array.length(); i++) {
                    JSONObject obj = array.getJSONObject(i);
                    HashMap<String, String> cat = new HashMap<>();
                    cat.put("id", obj.getString("_id"));
                    cat.put("name_ar", obj.getString("name_ar"));
                    cat.put("name_en", obj.getString("name_en"));
                    cat.put("icon", obj.optString("icon", "📚"));
                    cat.put("color", obj.optString("color", "#3b82f6"));
                    cat.put("count", String.valueOf(getTermsCountByCategory(obj.getString("_id"))));
                    categories.add(cat);
                }
            } catch (Exception e) {}
        }
        
        // ==================== تحميل المصطلحات الأساسية ====================
        String termsJson = prefs.getString(KEY_TERMS, "");
        if (!termsJson.isEmpty()) {
            try {
                JSONArray array = new JSONArray(termsJson);
                for (int i = 0; i < array.length(); i++) {
                    JSONObject obj = array.getJSONObject(i);
                    HashMap<String, String> term = new HashMap<>();
                    term.put("id", obj.getString("_id"));
                    term.put("term_ar", obj.getString("term_ar"));
                    term.put("term_en", obj.getString("term_en"));
                    term.put("definition_ar", obj.optString("definition_ar", "لا يوجد تعريف"));
                    term.put("definition_en", obj.optString("definition_en", "No definition"));
                    term.put("example_code", obj.optString("example_code", ""));
                    term.put("category_id", obj.getString("category_id"));
                    term.put("language", obj.optString("language", "General"));
                    allTerms.add(term);
                }
            } catch (Exception e) {}
        }
        
        // ==================== تحميل المصطلحات مع الحقول الإضافية ====================
        if (!termsJson.isEmpty()) {
            try {
                JSONArray array = new JSONArray(termsJson);
                for (int i = 0; i < array.length(); i++) {
                    JSONObject obj = array.getJSONObject(i);
                    HashMap<String, Object> term = new HashMap<>();
                    term.put("id", obj.getString("_id"));
                    term.put("term_ar", obj.getString("term_ar"));
                    term.put("term_en", obj.getString("term_en"));
                    term.put("definition_ar", obj.optString("definition_ar", ""));
                    term.put("definition_en", obj.optString("definition_en", ""));
                    term.put("example_code", obj.optString("example_code", ""));
                    term.put("category_id", obj.getString("category_id"));
                    term.put("language", obj.optString("language", ""));
                    
                    // قراءة الحقول الإضافية
                    if (obj.has("extra_fields")) {
                        JSONArray extraArray = obj.getJSONArray("extra_fields");
                        ArrayList<HashMap<String, String>> extraFields = new ArrayList<>();
                        for (int j = 0; j < extraArray.length(); j++) {
                            JSONObject extra = extraArray.getJSONObject(j);
                            HashMap<String, String> field = new HashMap<>();
                            field.put("type", extra.getString("type"));
                            field.put("title", extra.optString("title", ""));
                            field.put("content", extra.optString("content", ""));
                            if (extra.has("language")) {
                                field.put("language", extra.getString("language"));
                            }
                            extraFields.add(field);
                        }
                        term.put("extra_fields", extraFields);
                    } else {
                        term.put("extra_fields", new ArrayList<HashMap<String, String>>());
                    }
                    allTermsWithExtras.add(term);
                }
            } catch (Exception e) {}
        }
        
        // ==================== تحميل اللغات ====================
        String languagesJson = prefs.getString(KEY_LANGUAGES, "");
        if (!languagesJson.isEmpty()) {
            try {
                JSONArray array = new JSONArray(languagesJson);
                for (int i = 0; i < array.length(); i++) {
                    JSONObject obj = array.getJSONObject(i);
                    HashMap<String, String> lang = new HashMap<>();
                    lang.put("slug", obj.getString("slug"));
                    lang.put("name_ar", obj.getString("name_ar"));
                    lang.put("name_en", obj.getString("name_en"));
                    lang.put("icon", obj.optString("icon", "💻"));
                    lang.put("color", obj.optString("color", "#3b82f6"));
                    lang.put("description_ar", obj.optString("description_ar", ""));
                    lang.put("description_en", obj.optString("description_en", ""));
                    languages.add(lang);
                }
            } catch (Exception e) {}
        }
        
        // ==================== تحميل الدروس الأساسية ====================
        String lessonsJson = prefs.getString(KEY_LESSONS, "");
        if (!lessonsJson.isEmpty()) {
            try {
                JSONArray array = new JSONArray(lessonsJson);
                for (int i = 0; i < array.length(); i++) {
                    JSONObject obj = array.getJSONObject(i);
                    HashMap<String, String> lesson = new HashMap<>();
                    lesson.put("id", obj.getString("_id"));
                    lesson.put("title_ar", obj.getString("title_ar"));
                    lesson.put("title_en", obj.optString("title_en", ""));
                    lesson.put("description_ar", obj.optString("description_ar", ""));
                    lesson.put("description_en", obj.optString("description_en", ""));
                    lesson.put("language_id", obj.getString("language_id"));
                    lesson.put("order", String.valueOf(obj.optInt("order", 1)));
                    lessons.add(lesson);
                }
            } catch (Exception e) {}
        }
        
        // ==================== تحميل الدروس مع الأقسام ====================
        if (!lessonsJson.isEmpty()) {
            try {
                JSONArray array = new JSONArray(lessonsJson);
                for (int i = 0; i < array.length(); i++) {
                    JSONObject obj = array.getJSONObject(i);
                    HashMap<String, Object> lesson = new HashMap<>();
                    lesson.put("id", obj.getString("_id"));
                    lesson.put("title_ar", obj.getString("title_ar"));
                    lesson.put("title_en", obj.optString("title_en", ""));
                    lesson.put("description_ar", obj.optString("description_ar", ""));
                    lesson.put("description_en", obj.optString("description_en", ""));
                    lesson.put("language_id", obj.getString("language_id"));
                    lesson.put("order", obj.optInt("order", 1));
                    
                    // قراءة أقسام الدرس
                    if (obj.has("sections")) {
                        JSONArray sectionsArray = obj.getJSONArray("sections");
                        ArrayList<HashMap<String, String>> sections = new ArrayList<>();
                        for (int j = 0; j < sectionsArray.length(); j++) {
                            JSONObject section = sectionsArray.getJSONObject(j);
                            HashMap<String, String> sec = new HashMap<>();
                            sec.put("type", section.getString("type"));
                            sec.put("title", section.optString("title", ""));
                            sec.put("content", section.optString("content", ""));
                            if (section.has("language")) {
                                sec.put("language", section.getString("language"));
                            }
                            sections.add(sec);
                        }
                        lesson.put("sections", sections);
                    } else {
                        lesson.put("sections", new ArrayList<HashMap<String, String>>());
                    }
                    allLessonsWithSections.add(lesson);
                }
            } catch (Exception e) {}
        }
    }
    
    private int getTermsCountByCategory(String categoryId) {
        int count = 0;
        for (HashMap<String, String> term : allTerms) {
            String catId = term.get("category_id");
            if (catId != null && catId.equals(categoryId)) {
                count++;
            }
        }
        return count;
    }
    
    // ==================== دوال الحصول على البيانات الأساسية ====================
    
    public ArrayList<HashMap<String, String>> getCategories() {
        return categories;
    }
    
    public int getTotalTerms() {
        return allTerms.size();
    }
    
    public ArrayList<HashMap<String, String>> getTermsByCategory(String categoryId) {
        ArrayList<HashMap<String, String>> result = new ArrayList<>();
        for (HashMap<String, String> term : allTerms) {
            String catId = term.get("category_id");
            if (catId != null && catId.equals(categoryId)) {
                result.add(term);
            }
        }
        return result;
    }
    
    public HashMap<String, String> getTermById(String termId) {
        for (HashMap<String, String> term : allTerms) {
            String id = term.get("id");
            if (id != null && id.equals(termId)) {
                return term;
            }
        }
        return null;
    }
    
    public ArrayList<HashMap<String, String>> getLanguages() {
        return languages;
    }
    
    public ArrayList<HashMap<String, String>> getLessons() {
        return lessons;
    }
    
    public ArrayList<HashMap<String, String>> getLessonsByLanguage(String languageSlug) {
        ArrayList<HashMap<String, String>> result = new ArrayList<>();
        for (HashMap<String, String> lesson : lessons) {
            String langId = lesson.get("language_id");
            if (langId != null && langId.equals(languageSlug)) {
                result.add(lesson);
            }
        }
        return result;
    }
    
    public HashMap<String, String> getLanguageBySlug(String slug) {
        for (HashMap<String, String> lang : languages) {
            if (lang.get("slug").equals(slug)) {
                return lang;
            }
        }
        return null;
    }
    
    public ArrayList<HashMap<String, String>> getAllTerms() {
        return allTerms;
    }
    
    // ==================== دوال الحصول على البيانات الموسعة ====================
    
    public ArrayList<HashMap<String, Object>> getAllTermsWithExtras() {
        return allTermsWithExtras;
    }
    
    public ArrayList<HashMap<String, Object>> getAllLessonsWithSections() {
        return allLessonsWithSections;
    }
    
    public HashMap<String, Object> getTermWithExtrasById(String termId) {
        for (HashMap<String, Object> term : allTermsWithExtras) {
            String id = (String) term.get("id");
            if (id != null && id.equals(termId)) {
                return term;
            }
        }
        return null;
    }
    
    public ArrayList<HashMap<String, Object>> getTermsByCategoryWithExtras(String categoryId) {
        ArrayList<HashMap<String, Object>> result = new ArrayList<>();
        for (HashMap<String, Object> term : allTermsWithExtras) {
            String catId = (String) term.get("category_id");
            if (catId != null && catId.equals(categoryId)) {
                result.add(term);
            }
        }
        return result;
    }
    
    public ArrayList<HashMap<String, Object>> getLessonsByLanguageWithSections(String languageSlug) {
        ArrayList<HashMap<String, Object>> result = new ArrayList<>();
        for (HashMap<String, Object> lesson : allLessonsWithSections) {
            String langId = (String) lesson.get("language_id");
            if (langId != null && langId.equals(languageSlug)) {
                result.add(lesson);
            }
        }
        return result;
    }
    
    public String getLastUpdateTime() {
        long time = prefs.getLong(KEY_LAST_UPDATE, 0);
        if (time > 0) {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
            return sdf.format(new java.util.Date(time));
        }
        return "--";
    }
    
    public String getDataVersion() {
        return prefs.getString(KEY_DATA_VERSION, "1.0.0");
    }
    
    public boolean hasLocalData() {
        String categoriesJson = prefs.getString(KEY_CATEGORIES, "");
        return !categoriesJson.isEmpty();
    }
}