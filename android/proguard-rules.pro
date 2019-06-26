# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-keepattributes *Annotation*
-keepattributes Signature
-keepattributes Exceptions
-keepattributes EnclosingMethod
-keepattributes InnerClasses

# Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception
-keep class com.crashlytics.** { *; }
-dontwarn com.crashlytics.**
-dontwarn io.fabric.sdk.android.**

### Exoplayer2
-dontwarn com.google.android.exoplayer2.**

### Kotlin Coroutine
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

-dontwarn androidx.room.paging.LimitOffsetDataSource

-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}

# JSR 305 annotations are for embedding nullability information.
-dontwarn javax.annotation.**

-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn org.conscrypt.**
-dontwarn retrofit2.Platform$Java8
# A resource is loaded with a relative path so the package of this class must be preserved.
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase
# OkHttp platform used only on JVM and when Conscrypt dependency is available.
-dontwarn okhttp3.internal.platform.ConscryptPlatform
# Animal Sniffer compileOnly dependency to ensure APIs are compatible with older versions of Java.
-dontwarn org.codehaus.mojo.animal_sniffer.*

-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.stream.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

-dontwarn com.google.android.material.**

-keep class onlymash.flexbooru.entity.** { *; }
-keep class onlymash.flexbooru.respository.** { *; }

#https://github.com/Tickaroo/tikxml/blob/master/docs/AnnotatingModelClasses.md
-keep class com.tickaroo.tikxml.** { *; }
-keep @com.tickaroo.tikxml.annotation.Xml public class *
-keep class **$$TypeAdapter { *; }
-keepclasseswithmembernames class * {
    @com.tickaroo.tikxml.* <fields>;
}
-keepclasseswithmembernames class * {
    @com.tickaroo.tikxml.* <methods>;
}
-dontwarn com.tickaroo.tikxml.**

-keep class com.android.vending.billing.**

-dontnote kotlinx.serialization.SerializationKt
-keep,includedescriptorclasses class onlymash.flexbooru.saucenao.model.**$$serializer { *; }
-keepclassmembers class onlymash.flexbooru.saucenao.model.** {
    *** Companion;
}
-keepclasseswithmembers class onlymash.flexbooru.saucenao.model.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class onlymash.flexbooru.trecemoe.model.**$$serializer { *; }
-keepclassmembers class onlymash.flexbooru.trecemoe.model.** {
    *** Companion;
}
-keepclasseswithmembers class onlymash.flexbooru.trecemoe.model.** {
    kotlinx.serialization.KSerializer serializer(...);
}