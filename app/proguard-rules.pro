-dontobfuscate

-dontwarn okio.**
-keep class okhttp3.internal.**
-dontwarn javax.annotation.Nullable
-dontwarn javax.annotation.ParametersAreNonnullByDefault
-dontwarn org.conscrypt.OpenSSLProvider
-dontwarn org.conscrypt.Conscrypt

-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception

-dontwarn com.mikepenz.aboutlibraries.ui.item.HeaderItem
-dontwarn com.pavelsikun.vintagechroma.ChromaPreferenceCompat