# Add project specific Proguard/R8 rules here.

# Room rules
-keep class androidx.room.RoomDatabase { *; }
-dontwarn androidx.room.paging.**
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-keepclassmembers class * {
    @androidx.room.TypeConverter *;
}
-keep class com.vinay.monthlylekka.data.** { *; }

# Kotlinx Serialization & Parcelize rules
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}
-keepclassmembers class * {
    @kotlinx.serialization.Serializable *;
}
-keepclassmembers class *$$serializer {
    *** INSTANCE;
}

# Preserve NavKey / Routes
-keep class com.vinay.monthlylekka.ui.Route** { *; }
-keep class * implements androidx.navigation3.runtime.NavKey { *; }

# Moshi rules
-keep class com.squareup.moshi.** { *; }
-keepclasseswithmembers class * {
    @com.squareup.moshi.Json *;
}
-keepclassmembers class * {
    @com.squareup.moshi.ToJson *;
    @com.squareup.moshi.FromJson *;
}
