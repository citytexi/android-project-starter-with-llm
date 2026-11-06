# App-specific R8 rules. Hilt, Retrofit, OkHttp, Room and kotlinx.serialization ship
# their own consumer rules; :core:network adds rules for its @Serializable models.

# Navigation 3 saves the back stack by class name and looks the serializer up by
# reflection (NavKeySerializer). Keep every NavKey's name and its generated serializer.
-keepnames class * implements androidx.navigation3.runtime.NavKey
-keepclassmembers class * implements androidx.navigation3.runtime.NavKey {
    public static ** INSTANCE;
    public static ** Companion;
    *** serializer(...);
}
