# M. Esm Clinic Pro

نسخة Android Native احترافية كنقطة انطلاق قوية لتطبيق ملفات المرضى في العيادة.

## الموجود في النسخة
- Kotlin + Jetpack Compose
- Room database للتخزين المحلي
- AuthStore لحفظ بيانات الدخول الخاصة بك بدل admin/1234
- زر دخول بالبصمة مهيأ داخل الواجهة
- Home: بحث / إضافة
- إضافة الملف على مرحلتين: بيانات ثم صور
- شاشة View احترافية: البيانات أعلى الصفحة والصور مباشرة تحتها
- سحب يمين/يسار للتنقل بين الصور
- فصل صور الحالة عن صور الروشتة
- حفظ الصور داخل app-private storage لضمان ثباتها

## ملاحظات مهمة
- المشروع جاهز كهيكل قوي وقابل للتطوير داخل Android Studio.
- تفعيل BiometricPrompt الكامل يحتاج ربط جلسة البصمة داخل MainActivity في خطوة أخيرة أثناء التطوير.
- يفضل تشغيل Sync Gradle ثم Build APK من Android Studio.

## الخطوات
1. افتح المشروع في Android Studio Hedgehog أو أحدث.
2. Sync Gradle.
3. Run على جهاز Android أو Emulator.
4. Build > Build APK(s).

## بيانات الدخول الافتراضية
- username: admin
- password: 1234

أول مرة بعد الدخول يمكنك تغييرها لبياناتك الخاصة.
