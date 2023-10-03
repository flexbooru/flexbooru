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

-keep class com.android.vending.billing.**

# Kotlin serialization

# Keep `Companion` object fields of serializable classes.
# This avoids serializer lookup through `getDeclaredClasses` as done for named companion objects.
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}

# Keep `serializer()` on companion objects (both default and named) of serializable classes.
-if @kotlinx.serialization.Serializable class ** {
    static **$* *;
}
-keepclassmembers class <2>$<3> {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep `INSTANCE.serializer()` of serializable objects.
-if @kotlinx.serialization.Serializable class ** {
    public static ** INSTANCE;
}
-keepclassmembers class <1> {
    public static <1> INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}

# @Serializable and @Polymorphic are used at runtime for polymorphic serialization.
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault

# Serializer for classes with named companion objects are retrieved using `getDeclaredClasses`.
# If you have any, uncomment and replace classes with those containing named companion objects.
#-keepattributes InnerClasses # Needed for `getDeclaredClasses`.
#-if @kotlinx.serialization.Serializable class
#com.example.myapplication.HasNamedCompanion, # <-- List serializable classes with named companions.
#com.example.myapplication.HasNamedCompanion2
#{
#    static **$* *;
#}
#-keepnames class <1>$$serializer { # -keepnames suffices; class is kept when serializer() is kept.
#    static <1>$$serializer INSTANCE;
#}

# kotlinx-serialization-json specific. Add this if you have java.lang.NoClassDefFoundError kotlinx.serialization.json.JsonObjectSerializer
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keepclassmembers @kotlinx.serialization.Serializable class onlymash.flexbooru.data.model.** {
    # lookup for plugin generated serializable classes
    *** Companion;
    # lookup for serializable objects
    *** INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclassmembers @kotlinx.serialization.Serializable class onlymash.flexbooru.common.saucenao.model.** {
    # lookup for plugin generated serializable classes
    *** Companion;
    # lookup for serializable objects
    *** INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclassmembers @kotlinx.serialization.Serializable class onlymash.flexbooru.common.tracemoe.model.** {
    # lookup for plugin generated serializable classes
    *** Companion;
    # lookup for serializable objects
    *** INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}
# lookup for plugin generated serializable classes
-if @kotlinx.serialization.Serializable class onlymash.flexbooru.data.model.**
-keepclassmembers class onlymash.flexbooru.data.model.<1>$Companion {
    kotlinx.serialization.KSerializer serializer(...);
}
-if @kotlinx.serialization.Serializable class onlymash.flexbooru.common.saucenao.model.**
-keepclassmembers class onlymash.flexbooru.common.saucenao.model.<1>$Companion {
    kotlinx.serialization.KSerializer serializer(...);
}
-if @kotlinx.serialization.Serializable class onlymash.flexbooru.common.tracemoe.model.**
-keepclassmembers class onlymash.flexbooru.common.tracemoe.model.<1>$Companion {
    kotlinx.serialization.KSerializer serializer(...);
}

# Serialization supports named companions but for such classes it is necessary to add an additional rule.
# This rule keeps serializer and serializable class from obfuscation. Therefore, it is recommended not to use wildcards in it, but to write rules for each such class.
-keep class onlymash.flexbooru.data.model.SerializableClassWithNamedCompanion$$serializer {
    *** INSTANCE;
}
-keep class onlymash.flexbooru.common.saucenao.model.SerializableClassWithNamedCompanion$$serializer {
    *** INSTANCE;
}
-keep class onlymash.flexbooru.common.tracemoe.model.SerializableClassWithNamedCompanion$$serializer {
    *** INSTANCE;
}

-keep public enum nl.adaptivity.xmlutil.EventType {*;}

-dontwarn com.google.android.datatransport.runtime.ForcedSender
-dontwarn org.slf4j.**