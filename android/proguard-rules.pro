
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

# jvm-libp2p passes these classes to Netty's ReflectiveChannelFactory, which
# creates TCP channels through their pub「方案选单」lic no-argument constructors.
-keepclassmembers class io.netty.channel.socket.nio.NioServerSocketChannel {
    public <init>();
}
-keepclassmembers class io.netty.channel.socket.nio.NioSocketChannel {
    public <init>();
}

# Netty checks this inherited runtime annotation to decide whether one handler
# instance may be installed into multiple channel pipelines.
-keep @interface io.netty.channel.ChannelHandler$Sharable
-keep class io.netty.channel.ChannelInitializer

# Netty registers these methods with ResourceLeakDetector by their string names.
# R8 can otherwise inline and remove them while leaving the reflective lookup intact.
-keepclassmembers class io.netty.util.ReferenceCountUtil {
    public static *** touch(...);
}
-keepclassmembers class io.netty.buffer.AbstractByteBufAllocator {
    *** toLeakAwareBuffer(...);
}
-keepclassmembers class io.netty.buffer.AdvancedLeakAwareByteBuf {
    *** touch(...);
    static void recordLeakNonRefCountingOperation(...);
}

# Netty infers handler and resolver types by walking generic superclasses at
# runtime. R8 full mode only retains Signature attributes when both endpoints
# of the generic relationship are kept.
-keep class io.netty.channel.SimpleChannelInboundHandler
-keep class io.libp2p.multistream.Negotiator$GenericHandler
-keep class io.libp2p.multistream.Negotiator$RequesterHandler
-keep class io.libp2p.multistream.Negotiator$ResponderHandler
-keep class io.libp2p.protocol.ProtocolMessageHandlerAdapter
-keep class io.libp2p.security.noise.NoiseIoHandshake

-keep class io.netty.handler.codec.MessageToByteEncoder
-keep class io.netty.handler.codec.ByteToMessageCodec$Encoder
-keep class io.netty.handler.codec.ByteToMessageCodec
-keep class io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender

-keep class io.libp2p.mux.mplex.MplexFrameCodec
-keep class io.libp2p.mux.mplex.MplexFrame
-keep class io.libp2p.mux.yamux.YamuxFrameCodec
-keep class io.libp2p.mux.yamux.YamuxFrame

-keep class io.netty.handler.codec.MessageToMessageCodec
-keep class io.libp2p.etc.util.netty.StringSuffixCodec
-keep class io.libp2p.security.noise.NoiseXXCodec

-keep class io.netty.handler.codec.MessageToMessageDecoder
-keep class io.netty.handler.codec.protobuf.ProtobufDecoder
-keep class io.netty.handler.codec.string.StringDecoder

-keep class io.netty.handler.codec.MessageToMessageEncoder
-keep class io.libp2p.etc.util.netty.SplitEncoder
-keep class io.netty.handler.codec.LengthFieldPrepender
-keep class io.netty.handler.codec.protobuf.ProtobufEncoder
-keep class io.netty.handler.codec.string.StringEncoder

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
# names. jvm-libp2p uses EC for its local identity and RSA for legacy remote identities.
-keep class org.bouncycastle.jcajce.provider.asymmetric.EC$* { *; }
-keep class org.bouncycastle.jcajce.provider.asymmetric.ec.** { *; }
-keep class org.bouncycastle.jcajce.provider.asymmetric.RSA$* { *; }
-keep class org.bouncycastle.jcajce.provider.asymmetric.rsa.** { *; }

# Optional JVM-only backends referenced by Apache HttpClient and Netty are unavailable on Android.
-dontwarn javax.naming.**
-dontwarn org.ietf.jgss.**
-dontwarn org.apache.log4j.**
-dontwarn org.apache.logging.log4j.**
-dontwarn reactor.blockhound.integration.BlockHoundIntegration
