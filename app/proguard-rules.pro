# kotlinx.serialization (Navigations-Routen, später Backup/Import)
-keepattributes *Annotation*, InnerClasses
-keepclassmembers class de.luis.bulktracker.** {
    *** Companion;
}
-keepclasseswithmembers class de.luis.bulktracker.** {
    kotlinx.serialization.KSerializer serializer(...);
}
