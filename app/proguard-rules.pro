# Keep rules para Retrofit / kotlinx.serialization / Coil (se refinan con el primer build release).
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes AnnotationDefault

# kotlinx.serialization: mantener serializers.
-keep,includedescriptorclasses class app.ocnotes.**$$serializer { *; }
-keepclassmembers class app.ocnotes.** {
    *** Companion;
}
-keepclasseswithmembers class app.ocnotes.** {
    kotlinx.serialization.KSerializer serializer(...);
}
