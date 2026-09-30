# kotlinx.serialization — preserve generated $serializer classes and companions
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class dev.pdv.yamulite.tv.**$$serializer { *; }
-keepclassmembers class dev.pdv.yamulite.tv.** {
    *** Companion;
}
-keepclasseswithmembers class dev.pdv.yamulite.tv.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Retrofit — keep annotated interface methods
-keepclasseswithmembers interface dev.pdv.yamulite.tv.** {
    @retrofit2.http.* <methods>;
}
-keepattributes Signature, Exceptions

# OkHttp — suppress noisy warnings from platform detection
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# ZXing QR generation only uses core encoding; no reflection to preserve.
-dontwarn com.google.zxing.**
