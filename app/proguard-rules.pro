# Add project specific ProGuard rules here.

# Keep Jsoup
-keep public class org.jsoup.** { *; }
-keeppackagenames org.jsoup.**

# Keep OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# Keep Coil
-keep class coil.** { *; }

# Keep data models (used via reflection by Compose)
-keep class bd.du.bangla.shahittopotrika.data.model.** { *; }

# Kotlin coroutines
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# Compose
-keep class androidx.compose.** { *; }

# General Android
-dontwarn javax.annotation.**
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes Exceptions
