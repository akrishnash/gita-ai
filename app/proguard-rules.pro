# Add project specific ProGuard rules here.
-keep class com.gitaaikrishna.app.data.** { *; }
-keep class com.gitaaikrishna.app.logic.** { *; }
-keep class com.gitaaikrishna.app.kotlinmodel.** { *; }
-keep class com.gitaaikrishna.app.network.** { *; }
-keep class com.gitaaikrishna.app.logic.HistoryEntry { *; }

# Prevent BuildConfig fields (including API keys) from being stripped
-keep class com.gitaaikrishna.app.BuildConfig { *; }

# Razorpay SDK
-keep class com.razorpay.** { *; }
-keepclassmembers class com.razorpay.** { *; }
-dontwarn com.razorpay.**

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# OkHttp + Retrofit
-dontwarn okhttp3.**
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# Firebase
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
