-keepattributes SourceFile,LineNumberTable        # Keep file names and line numbers.
-keep public class * extends java.lang.Exception  # Optional: Keep custom exceptions.

# TODO If crash reports can support this use this to hide the original source file name.
#-renamesourcefileattribute SourceFile