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