
-dontobfuscate

# Resource IDs are translated back to asset names through reflection.
-keep class com.corrodinggames.rts.R$* {
    public static <fields>;
}

# WebRTC's native library creates Java bridge objects and invokes their callbacks through JNI.
# The published AAR does not provide consumer rules for these entry points.
-keep class org.webrtc.** { *; }
-keep class org.jni_zero.** { *; }
-dontwarn org.jni_zero.**

# Legacy settings are discovered and accessed dynamically by their public field names.
-keepclassmembers class com.corrodinggames.rts.gameFramework.SettingsEngine {
    public <fields>;
}

# Key binding maps are built by reflecting over these constant fields.
-keep class com.corrodinggames.rts.gameFramework.utility.SlickToAndroidKeycodes$* {
    public static final int *;
}

# Logic boolean parameters are discovered from annotated fields and setters.
-keepclassmembers class com.corrodinggames.rts.game.units.custom.logicBooleans.** {
    @com.corrodinggames.rts.game.units.custom.logicBooleans.LogicBoolean$Parameter <fields>;
    @com.corrodinggames.rts.game.units.custom.logicBooleans.LogicBoolean$Parameter <methods>;
}

# Kotlin reflection is used by the mod INI loader.
-keepattributes RuntimeVisibleAnnotations,RuntimeInvisibleAnnotations,AnnotationDefault,Signature,InnerClasses,EnclosingMethod
-keep class kotlin.Metadata { *; }

# Kotlin stdlib is shared with dynamically loaded mods (DexClassLoader with host as parent).
# R8 must not strip it even when host call sites are optimized away.
-keep class kotlin.** { *; }
-keepclassmembers class kotlin.** { *; }

# Mods implement these public entry points outside the application APK.
-keep public class io.github.rwx.mod.api.** { public protected *; }
-keep public class io.github.rwx.mod.Mod { public protected *; }
-keep public class io.github.rwx.mod.JvmMod { public protected *; }

# Keep enum names used by configuration parsing.
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Bouncy Castle registers its JCA algorithms through mapping and implementation class
# names. Keep EC and RSA providers used for peer identities and signatures.
-keep class org.bouncycastle.jcajce.provider.asymmetric.EC$* { *; }
-keep class org.bouncycastle.jcajce.provider.asymmetric.ec.** { *; }
-keep class org.bouncycastle.jcajce.provider.asymmetric.RSA$* { *; }
-keep class org.bouncycastle.jcajce.provider.asymmetric.rsa.** { *; }

# Optional JVM-only backends referenced by Apache HttpClient are unavailable on Android.
-dontwarn javax.naming.**
-dontwarn org.ietf.jgss.**
-dontwarn org.apache.log4j.**
-dontwarn org.apache.logging.log4j.**
-dontwarn reactor.blockhound.integration.BlockHoundIntegration
