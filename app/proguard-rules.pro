# ProGuard Rules for AetherOS Release Build
# ==============================================================================

# General / Attributes Preservation
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod, SourceFile, LineNumberTable
-renamesourcefileattribute SourceFile

# --- Moshi & JSON Data Models ---
# Preserve all data model classes and their fields so JSON serialization/deserialization doesn't break
-keep class com.example.data.model.** { *; }
-keepclassmembers class com.example.data.model.** { *; }

# Preserve Moshi classes and generated adapters
-keep class com.squareup.moshi.** { *; }
-keep interface com.squareup.moshi.** { *; }
-dontwarn com.squareup.moshi.**

# Preserve classes annotated with Moshi annotations
-keep @com.squareup.moshi.JsonClass class * { *; }
-keep @com.squareup.moshi.JsonQualifier @interface * { *; }
-keepclassmembers class * {
    @com.squareup.moshi.FromJson *;
    @com.squareup.moshi.ToJson *;
    @com.squareup.moshi.Json *;
}

# Preserve Moshi Kotlin reflection metadata
-keep class kotlin.reflect.jvm.internal.** { *; }
-dontwarn kotlin.reflect.**

# --- Retrofit & OkHttp ---
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-keepattributes *Annotation*, RuntimeVisibleAnnotations, RuntimeInvisibleAnnotations

# OkHttp & Okio
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# --- Room Database ---
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-keep class com.example.data.local.** { *; }
-dontwarn androidx.room.paging.**

# --- Kotlin Coroutines ---
-dontwarn kotlinx.coroutines.**
-keepclassmembers class kotlinx.coroutines.** { *; }
