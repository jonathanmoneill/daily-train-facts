# Keep Room entities and DAOs
-keep class com.gfd.dailytrainfacts.data.** { *; }

# Keep WorkManager Workers
-keep class * extends androidx.work.ListenableWorker { *; }

# Keep Glance AppWidgets
-keep class * extends androidx.glance.appwidget.GlanceAppWidget { *; }
-keep class * extends androidx.glance.appwidget.GlanceAppWidgetReceiver { *; }

# Preserve line numbers for Crashlytics
-keepattributes SourceFile,LineNumberTable
