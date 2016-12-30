# To enable ProGuard in your project, edit project.properties
# to define the proguard.config property as described in that file.
#
# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in ${sdk.dir}/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the ProGuard
# include property in project.properties.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-dontskipnonpubliclibraryclassmembers
-verbose
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*
-keepattributes *Annotation*,EnclosingMethod
-keepattributes Signature
-ignorewarnings
-optimizations Dmaximum.inlined.code.length=4000

-keepattributes *Annotation*
-keepattributes Signature

-libraryjars libs/armeabi/libBugly.so
-libraryjars libs/armeabi/libSDK_topvdn.so
#-libraryjars libs/armeabi/libCloudService.so
-libraryjars libs/armeabi/libcrypto_topvdn.so
-libraryjars libs/armeabi/libffmpeg_topvdn.so
-libraryjars libs/armeabi/libjplayer_topvdn.so
-libraryjars libs/armeabi/libssl_topvdn.so
-libraryjars libs/armeabi/liblocSDK6a.so

#-libraryjars libs/bugly_1.2.3.6__release.jar
#-libraryjars libs/gson-2.2.4.jar
#-libraryjars libs/locSDK_6.13.jar
#base lib
-libraryjars  ../com.lingyang.base/libs/jackson-core-asl-1.8.5.jar
-libraryjars  ../com.lingyang.base/libs/jackson-mapper-asl-1.8.5.jar
-libraryjars  ../com.lingyang.base/libs/android-support-annotations.jar
#sdk lib
#-libraryjars  ../com.lingyang.sdk/libs/gson-2.2.4.jar

-dontwarn android.support.v4.**
-dontwarn org.apache.commons.net.**
-dontwarn com.tencent.**
-dontwarn org.codehaus.jackson.**
-keepclasseswithmembernames class * {
    native <methods>;
}
-keepclasseswithmembernames class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}
-keepclasseswithmembernames class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context);
}
-dontshrink
-dontoptimize
-dontwarn android.webkit.WebView
-dontwarn java.nio.file.Files
-dontwarn java.nio.file.Path
-dontwarn java.nio.file.OpenOption
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement

-keep public class * extends android.app.Fragment
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service

-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class * extends android.support.v4.**
-keep public class * extends android.support.annotations.**
-keep public class com.android.vending.licensing.ILicensingService
-keep class android.annotation.** { *; }
-keep public class * extends android.app.Fragment
-keep public class * extends android.app.FragmentActivity
-keep public class * extends android.support.v4.app.FragmentActivity
-keep public class * extends android.support.v4.app.Fragment
-keep public class * extends android.os.Binder
-keep class * implements android.os.Parcelable {*;}
-keep class * implements java.io.Serializable {*;}

-keep public class com.tencent.bugly.**{*;}
-keep class com.lingyang.sdk.**{*;}
#-keep class com.lingyang.camera.**{ *;}
-keep class com.lingyang.camera.entity.**{*;}
-keep public class com.mikhaellopez.circularfillableloaders.**{*;}

-printmapping mapping.txt
-printseeds seeds.txt
-printusage unused.txt

-keep class com.newrelic.** { *; }

-dontwarn com.newrelic.**

-keepattributes Exceptions, Signature, InnerClasses