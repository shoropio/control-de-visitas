# Reglas ProGuard específicas de la aplicación.
# El minify está desactivado en esta primera versión.
-keepattributes Signature
-keep class androidx.room.** { *; }
-keep class com.shoropio.controlingreso.data.database.** { *; }