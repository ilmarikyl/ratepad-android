# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Preserve line numbers for debugging stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ========== Retrofit ==========
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeInvisibleAnnotations
-keepattributes RuntimeVisibleParameterAnnotations
-keepattributes RuntimeInvisibleParameterAnnotations

# Keep API interfaces and their methods - CRITICAL for Retrofit
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# Keep our API interface completely
-keep interface com.ilmariware.currencyconverterwidget.data.ExchangeRateApi { *; }
-keep,allowobfuscation,allowshrinking interface com.ilmariware.currencyconverterwidget.data.** { *; }

-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# Preserve generic signatures for Retrofit methods
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Keep generic type information for Response wrappers
-keep,allowobfuscation,allowshrinking class retrofit2.Response { *; }
-keepclassmembers class retrofit2.Response {
    *;
}

# ========== OkHttp ==========
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# ========== Gson ==========
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses
# Critical for Gson reflection
-keepattributes RuntimeVisibleAnnotations
-keepattributes AnnotationDefault
-dontwarn sun.misc.**
-dontwarn java.lang.reflect.**

# Keep Gson core classes
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Keep all data models for JSON parsing - prevent obfuscation completely
-keep,allowobfuscation,allowshrinking class com.ilmariware.currencyconverterwidget.data.models.** 
-keep class com.ilmariware.currencyconverterwidget.data.models.** { *; }
-keepclassmembers class com.ilmariware.currencyconverterwidget.data.models.** { 
    <init>(...);
    <fields>;
    <methods>;
}

# Gson specific classes
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken

# Keep all fields with SerializedName annotation
-keepclassmembers class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

# Keep generic signature for Map types used in models
-keepattributes Signature
-keep class * implements java.util.Map { *; }

# Application data classes - CRITICAL for Gson - NO OBFUSCATION
-keep class com.ilmariware.currencyconverterwidget.data.models.ExchangeRateResponse {
    *;
}
-keep class com.ilmariware.currencyconverterwidget.data.models.ExchangeRate {
    *;
}

# ========== Kotlin ==========
# Keep Kotlin metadata for reflection
-keep class kotlin.Metadata { *; }
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}

# Keep data class copy() methods and component functions
-keepclassmembers class * {
    *** copy(...);
    *** component*();
}

# ========== Kotlin Coroutines ==========
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-dontwarn kotlinx.coroutines.**
-keep class kotlinx.coroutines.** { *; }

# ========== Android Components ==========
# Keep AppWidgetProvider
-keep class * extends android.appwidget.AppWidgetProvider { *; }

# Keep all widget components
-keep class com.ilmariware.currencyconverterwidget.widget.** { *; }

# Keep activities
-keep class com.ilmariware.currencyconverterwidget.MainActivity { *; }
-keep class com.ilmariware.currencyconverterwidget.WidgetConfigurationActivity { *; }

# ========== WorkManager ==========
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.CoroutineWorker { *; }
-keep class androidx.work.impl.background.systemalarm.RescheduleReceiver

# ========== General Android ==========
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

-keepclassmembers class **.R$* {
    public static <fields>;
}

# Keep enum classes
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}