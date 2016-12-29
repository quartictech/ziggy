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
