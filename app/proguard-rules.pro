# Proguard rules for VALUE Timer app

# Keep Android Application & Service components
-keep class com.devvardhan.value.TimerApplication { *; }
-keep class com.devvardhan.value.MainActivity { *; }
-keep class com.devvardhan.value.service.TimerService { *; }

# Keep Data Models & Enums
-keepclassmembers enum com.devvardhan.value.model.** { *; }
-keep class com.devvardhan.value.model.** { *; }

# Keep DataStore Preferences
-keep class androidx.datastore.** { *; }

# Keep Coroutines
-keepattributes *Annotation*, InnerClasses, EnclosingMethod
-keepclassmembers class kotlinx.coroutines.** { *; }

# Jetpack Compose optimizations
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
}

