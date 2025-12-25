# Vouch SDK ProGuard Rules

# Keep public API
-keep public class expert.vouch.sdk.Vouch { *; }
-keep public class expert.vouch.sdk.models.** { *; }

# Keep data classes for Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep @Serializable data classes
-keep,includedescriptorclasses class expert.vouch.sdk.models.**$$serializer { *; }
-keepclassmembers class expert.vouch.sdk.models.** {
    *** Companion;
}
-keepclasseswithmembers class expert.vouch.sdk.models.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase
