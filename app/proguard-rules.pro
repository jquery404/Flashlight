# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /mfad/Library/Android/SDK/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Preserve line number information for debugging stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep custom views
-keep class com.jquery404.flashlight.main.** { *; }
-keep class com.jquery404.flashlight.custom.** { *; }
-keep class com.jquery404.flashlight.adapter.** { *; }

# Keep Parcelable implementations
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep Visualizer classes
-keep class android.media.audiofx.Visualizer { *; }

# Keep MediaPlayer
-keep class android.media.MediaPlayer { *; }

# Keep Camera classes (for Camera2/CameraX)
-keep class androidx.camera.** { *; }
-dontwarn androidx.camera.**

# Keep EasyPermissions
-keep class pub.devrel.easypermissions.** { *; }
-dontwarn pub.devrel.easypermissions.**

# Remove logging in release
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}
