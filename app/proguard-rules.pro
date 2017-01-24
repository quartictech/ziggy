# Workarounds for tooling brokenness (see http://stackoverflow.com/a/35742739/129570)
-dontnote android.net.http.*
-dontnote org.apache.commons.codec.**
-dontnote org.apache.http.**

# More workarounds (http://stackoverflow.com/a/39603548/129570)
-keepattributes EnclosingMethod
-keepattributes InnerClasses

# More workarounds (http://stackoverflow.com/a/29886914/129570)
-dontwarn okio.**
-dontwarn retrofit2.Platform$Java8
-dontwarn com.fasterxml.jackson.databind.**

# More workarounds (http://stackoverflow.com/a/33478972/129570)
-dontwarn sun.misc.Unsafe

# More workarounds (https://github.com/square/retrofit/issues/2034#issuecomment-259380094)
-dontwarn retrofit2.adapter.rxjava.CompletableHelper$**

# More workarounds
-dontnote kotlin.reflect.jvm.internal.KClassImpl$**

# SnakeYAML
-dontwarn org.yaml.snakeyaml.**
-dontwarn com.fasterxml.jackson.dataformat.yaml.snakeyaml.**

# Needed for Jackson not to be horrific
# http://stackoverflow.com/questions/27687128/how-to-setup-proguard-for-jackson-json-processor/28022792#28022792
-keepattributes *Annotation*,EnclosingMethod,Signature
-keepnames class com.fasterxml.jackson.** { *; }
 -dontwarn com.fasterxml.jackson.databind.**
 -keep class org.codehaus.** { *; }
 -keepclassmembers public final enum org.codehaus.jackson.annotate.JsonAutoDetect$Visibility {
 public static final org.codehaus.jackson.annotate.JsonAutoDetect$Visibility *; }
-keep public class your.class.** {
  public void set*(***);
  public *** get*();
}

# Kotlin stuff from here
# https://github.com/cypressious/KotlinReflectionProguard/blob/master/app/proguard-rules.pro
-dontwarn kotlin.**
-dontwarn org.w3c.dom.events.*
-dontwarn org.jetbrains.kotlin.di.InjectorForRuntimeDescriptorLoader

-keepattributes SourceFile,LineNumberTable

-keep class kotlin.** { *; }
#-keep class kotlin.reflect.** { *; }
#-keep class org.jetbrains.kotlin.** { *; }

-keepclassmembers,allowoptimization enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
    **[] $VALUES;
    public *;
}

-keepattributes InnerClasses
