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

######################################
# Application
######################################
#--------------------
# Keep entry point class
#--------------------

-keep class butterknife.** { *; }
-dontwarn butterknife.internal.**
-keep class **$$ViewBinder { *; }

-keepclasseswithmembernames class * {
    @butterknife.* <fields>;
}

-keepclasseswithmembernames class * {
    @butterknife.* <methods>;
}


-keep class org.mesibo.Messenger.StartUpActivity { *; }

-keepclasseswithmembernames class * {
    native <methods>;
}


-dontwarn com.mesibo.api.**
-dontwarn com.mesibo.contactutils.**

######################################
# android-support-v4
######################################
#--------------------
# some classes are used for method's descriptor
#--------------------
-keep class android.support.v4.**

-keep class android.support.v7.** { *; }

-keep class * extends android.support.v4.view.ActionProvider {
    public <init>(android.content.Context);
}

-dontwarn android.support.design.**
-keep class android.support.design.** { *; }
-keep interface android.support.design.** { *; }
-keep public class android.support.design.R$* { *; }

-keep public class android.support.v7.widget.** { *; }
-keep public class android.support.v7.internal.widget.** { *; }
-keep public class android.support.v7.internal.view.menu.** { *; }

-keep public class * extends android.support.v4.view.ActionProvider {
    public <init>(android.content.Context);
}

-keep interface android.support.v4.** { *; }
-keep interface android.support.v7.** { *; }
-keep class android.support.** { *; }

####################################################################  REMOVE WARNINGS


-dontwarn android.support.design.internal.**
-dontwarn com.google.android.gms.**
-dontwarn android.support.v4.**


-dontwarn org.webrtc.NetworkMonitorAutoDetect
-dontwarn android.net.Network
-keep class org.webrtc.** { *; }
######################################
# Google Play Services SDK
######################################
#--------------------
# http://developer.android.com/google/play-services/setup.html#Proguard
#--------------------
-keep class * extends java.util.ListResourceBundle {
#    protected Object[][] getContents();                                 # modified
    protected java.lang.Object[][] getContents();
}

-keep public class com.google.android.gms.common.internal.safeparcel.SafeParcelable {
    public static final *** NULL;
}

-keepnames @com.google.android.gms.common.annotation.KeepName class *
-keepclassmembernames class * {
    @com.google.android.gms.common.annotation.KeepName *;
}

-keepnames class * implements android.os.Parcelable {
    public static final ** CREATOR;
}


#--------------------
# com.google.android.gms.maps.internal.u
#--------------------
-dontnote com.google.android.gms.maps.internal.CreatorImpl
-dontnote com.google.android.gms.maps.internal.CreatorImplGmm6


#--------------------
# com.millennialmedia.google.gson.internal.UnsafeAllocator
#--------------------
-dontnote sun.misc.Unsafe


-keep class com.google.android.gms.common.ConnectionResult

-dontnote com.google.gson.internal.UnsafeAllocator
######################################
# AWS SDK
######################################
#--------------------
# http://mobile.awsblog.com/post/Tx2OC71PFCTC63E/Using-ProGuard-with-the-AWS-SDK-for-Android
#--------------------
-keep class org.apache.commons.logging.**               { *; }
#-keep class com.amazonaws.services.sqs.QueueUrlHandler  { *; }                   # modified

-keepattributes Signature,*Annotation*

-dontwarn org.apache.commons.logging.impl.**
-dontwarn org.apache.http.conn.scheme.**


#--------------------
# http://stackoverflow.com/questions/25284562/proguard-aws-s3-issue
#--------------------
#-keep class org.joda.time.tz.Provider                    { *; }                            # modified
#-keep class org.joda.time.tz.NameProvider                { *; }                            # modified
-keepattributes Signature,*Annotation*,EnclosingMethod



#--------------------
# dynamic reference from com.amazonaws.AmazonServiceException$ErrorType
#--------------------
#-keep class com.amazonaws.AmazonServiceException*


#--------------------
# com.amazonaws.services.s3.AmazonS3Client needed this
#--------------------

-keep class com.afollestad.materialdialogs.** {*;}

#Note: com.google.gson.internal.UnsafeAllocator accesses a declared field 'theUnsafe' dynamically
-keep class com.google.gson.internal.UnsafeAllocator { java.lang.reflect.Field theUnsafe; }
#Resulting Note: the configuration refers to the unknown field 'java.lang.reflect.Field theUnsafe' in class 'com.google.gson.internal.UnsafeAllocator'

-dontnote com.afollestad.materialdialogs.internal.MDTintHelper


#--------------------
# dynamic reference from com.amazonaws.org.apache.http.impl.auth.NTLMEngineImpl$NTLMMessage
# dynamic reference from com.amazonaws.org.apache.http.impl.auth.GGSSchemeBase
# dynamic reference from com.amazonaws.org.apache.http.impl.auth.BasicScheme
#--------------------


#--------------------
# dynamic reference from com.amazonaws.com.google.gson.internal.UnsafeAllocator
#--------------------
-dontnote sun.misc.Unsafe

##---------------Begin: proguard configuration for Gson  ----------
# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*

# Gson specific classes
-keep class sun.misc.Unsafe { *; }
#-keep class com.google.gson.stream.** { *; }

# Application classes that will be serialized/deserialized over Gson
-keep class com.google.gson.examples.android.model.** { *; }