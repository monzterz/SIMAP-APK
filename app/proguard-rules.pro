# Gson/Retrofit: pertahankan model DTO + interface API dari obfuscation R8
-keep class com.arffsnia.simap.data.dto.** { *; }
-keep interface com.arffsnia.simap.data.api.ApiService { *; }

# ML Kit barcode scanning — WAJIB keep, full-mode R8 merusak init-nya (NPE saat buka kamera)
-keep class com.google.mlkit.** { *; }
-keep class com.google.android.gms.internal.mlkit_vision_barcode.** { *; }
-dontwarn com.google.mlkit.**
-dontwarn com.google.android.gms.internal.mlkit_vision_barcode.**

# WorkManager + Room — R8 menghapus no-arg constructor WorkDatabase_Impl
# (NoSuchMethodException saat InitializationProvider) tanpa keep ini.
-keep class * extends androidx.room.RoomDatabase { <init>(); }
-keep @androidx.room.Entity class *
-keep class androidx.work.** { *; }
-keep class androidx.room.** { *; }
-keepattributes *Annotation*

-dontwarn okhttp3.**
-dontwarn retrofit2.**