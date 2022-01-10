
-keep class com.dalti.laposte.admin.entity.** { *; }
-keep class com.dalti.laposte.core.entity.** { *; }
-keep class dz.jsoftware95.queue.api.** { *; }
-keep class ** implements com.dalti.laposte.core.entity.ExternalAPI { *; }

-keepattributes SourceFile,LineNumberTable        # Keep file names and line numbers.
-keep public class * extends java.lang.Throwable  # Optional: Keep exceptions.