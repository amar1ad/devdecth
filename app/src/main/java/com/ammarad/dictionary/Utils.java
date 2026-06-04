package com.ammarad.dictionary;

import android.content.Context;
import android.content.Intent;

public class Utils {
    
    public static void shareApp(Context context, int categoriesCount) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, 
            " قاموس المبرمج الذكي\n" +
            "أول قاموس تفاعلي للمبرمجين!\n" +
            "يحتوي على " + categoriesCount + " تصنيف ومصطلحات برمجية.\n" +
            "حمله الآن: https://play.google.com/store/apps/details?id=" + context.getPackageName()
        );
        context.startActivity(Intent.createChooser(shareIntent, "مشاركة التطبيق"));
    }
}