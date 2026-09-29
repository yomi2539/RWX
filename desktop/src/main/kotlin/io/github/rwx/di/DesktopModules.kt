package io.github.rwx.di

import io.github.rwx.*
import io.github.rwx.app.AppOptions
import io.github.rwx.p2p.DesktopWebRtcTunnelProxy
import io.github.rwx.p2p.WebRtcTunnelProxy
import io.github.rwx.session.GameSession
import io.github.rwx.slick.SlickGameSession
import io.github.rwx.skia.SkiaGameSession
import org.koin.dsl.module

val desktopModule = module {
    WebRtcTunnelProxy.registerFactory { config -> DesktopWebRtcTunnelProxy(config) }
    single<PlatformBridge> { DesktopPlatformBridge() }
    single<PlatformStorage> { get<PlatformBridge>().storage }
    single<PreferenceStorage> { get<PlatformBridge>().preferenceStorage }
    single<AppMetadata> { get<PlatformBridge>().appMetadata }
    single<AppLogger> { get<PlatformBridge>().logger }
    single<CrashReporter> { get<PlatformBridge>().crashReporter }
    single { SlickGameSession(storage = get()) }
    single { SkiaGameSession(storage = get()) }
    single<GameSession> {
        val rendererId = runCatching { get<AppOptions>().backendId }.getOrNull()
        val stored = runCatching {
            get<PreferenceStorage>().preference(PREFERENCE_NAME).getString("desktopRenderBackend", "slick")
        }.getOrNull()
        when (DesktopRenderBackend.selectedId(rendererId, stored)) {
            DesktopRenderBackend.Skia -> get<SkiaGameSession>()
            DesktopRenderBackend.Slick -> get<SlickGameSession>()
        }
    }
}
