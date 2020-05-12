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
#noinspection ShrinkerUnresolvedReference

-keepattributes *Annotation*, Exceptions, EnclosingMethod, InnerClasses, Signature

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

### Kotlin Coroutine
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

-keep class com.google.android.gms.** { *; }

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

-keep class com.android.vending.billing.**

-dontnote kotlinx.serialization.SerializationKt
-keep,includedescriptorclasses class onlymash.flexbooru.data.model.**$$serializer { *; }
-keep,includedescriptorclasses class onlymash.flexbooru.common.saucenao.model.**$$serializer { *; }
-keep,includedescriptorclasses class onlymash.flexbooru.common.tracemoe.model.**$$serializer { *; }
-keepclassmembers class onlymash.flexbooru.data.model.** {
    *** Companion;
}
-keepclassmembers class onlymash.flexbooru.common.saucenao.model.** {
    *** Companion;
}
-keepclassmembers class onlymash.flexbooru.common.tracemoe.model.** {
    *** Companion;
}
-keepclasseswithmembers class onlymash.flexbooru.data.model.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclasseswithmembers class onlymash.flexbooru.common.saucenao.model** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclasseswithmembers class onlymash.flexbooru.common.tracemoe.model.** {
    kotlinx.serialization.KSerializer serializer(...);
}