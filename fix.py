import os
import xml.etree.ElementTree as ET

def fix_duplicates():
    # المسارات الافتراضية لملفات الموارد داخل مشروع أندرويد
    base_path = os.path.join('app', 'src', 'main', 'res', 'values')
    colors_path = os.path.join(base_path, 'colors.xml')
    styles_path = os.path.join(base_path, 'styles.xml')
    themes_path = os.path.join(base_path, 'themes.xml')

    print("=== بدء فحص وإصلاح ملفات الموارد ===")

    # 1. تجميع الأسماء المعرفة في colors.xml لتجنب تكرارها في styles.xml
    color_names_in_colors = set()
    if os.path.exists(colors_path):
        try:
            tree = ET.parse(colors_path)
            root = tree.getroot()
            for color in root.findall('color'):
                name = color.get('name')
                if name:
                    color_names_in_colors.add(name)
            print(f"[معلومة] تم العثور على {len(color_names_in_colors)} لون في ملف colors.xml")
        except Exception as e:
            print(f"[خطأ] تعذر قراءة ملف colors.xml: {e}")
    else:
        print("[تحذير] لم يتم العثور على ملف colors.xml في المسار المتوقع.")

    # 2. إصلاح ملف styles.xml (حذف عناصر <color> المكررة وحذف AppTheme إذا كان مكرراً)
    if os.path.exists(styles_path):
        try:
            # تسجيل النطاقات لتجنب تغيير الرموز البرمجية الخاصة بأندرويد
            ET.register_namespace('tools', 'http://schemas.android.com/tools') 
            tree = ET.parse(styles_path)
            root = tree.getroot()
            
            removed_colors_count = 0
            removed_themes_count = 0
            elements_to_remove = []
            
            # البحث عن عناصر <color> داخل styles.xml وحذفها إذا كانت موجودة في colors.xml
            for color in root.findall('color'):
                name = color.get('name')
                if name in color_names_in_colors:
                    elements_to_remove.append(color)
                    removed_colors_count += 1
            
            # البحث عن ستايل AppTheme لحذفه إذا كان موجوداً أيضاً في themes.xml
            if os.path.exists(themes_path):
                for style in root.findall('style'):
                    if style.get('name') == 'AppTheme':
                        elements_to_remove.append(style)
                        removed_themes_count += 1
            
            # إزالة العناصر المكررة المحددة
            for elem in elements_to_remove:
                root.remove(elem)
                
            if removed_colors_count > 0 or removed_themes_count > 0:
                # إعادة حفظ الملف بعد التعديل والتنظيف
                tree.write(styles_path, encoding='utf-8', xml_declaration=True)
                print(f"[نجاح] تم تحديث ملف styles.xml: حذف {removed_colors_count} ألوان مكررة و {removed_themes_count} ثيم مكرر.")
            else:
                print("[معلومة] ملف styles.xml نظيف ولا يحتوي على تكرارات مباشرة.")
                
        except Exception as e:
            print(f"[خطأ] حدثت مشكلة أثناء تعديل ملف styles.xml: {e}")
    else:
        print("[تحذير] لم يتم العثور على ملف styles.xml.")

    print("=== انتهت العملية بنجاح! يمكنك الآن عمل Clean و Rebuild للمشروع ===")

if __name__ == "__main__":
    # التأكد من أن السكريبت يعمل في المكان الصحيح بجانب مجلد app
    if os.path.exists('app'):
        fix_duplicates()
    else:
        print("[خطأ] لم يتم العثور على مجلد 'app' بجانب هذا السكريبت. يرجى نقل السكريبت وضعه بجانب مجلد app مباشرة.")

