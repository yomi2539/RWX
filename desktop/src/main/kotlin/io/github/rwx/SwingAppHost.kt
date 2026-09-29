package io.github.rwx

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposePanel
import com.corrodinggames.rts.gameFramework.SettingsEngine
import io.github.rwx.app.AppSession
import io.github.rwx.render.frame.GameViewport
import io.github.rwx.slick.SlickAwtGLCanvas
import io.github.rwx.slick.SlickCanvasHost
import io.github.rwx.slick.SlickFrameSnapshot
import io.github.rwx.slick.SlickSnapshotPanel
import io.github.rwx.skia.SkiaOverlayPanelHost
import io.github.rwx.skia.acceptsDirectSkiaInput
import io.github.rwx.skia.createSkiaGamePanel
import io.github.rwx.ui.AppScreen
import io.github.rwx.ui.AppUiState
import io.github.rwx.ui.DesktopComposeOverlay
import io.github.rwx.ui.installComposeOverlay
import io.github.rwx.ui.platform.createDesktopComposePanel
import org.lwjgl.opengl.awt.GLData
import java.awt.Canvas
import java.awt.Color
import java.awt.Dimension
import java.awt.GraphicsEnvironment
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicBoolean
import javax.swing.JFileChooser
import javax.swing.JFrame
import javax.swing.JLayeredPane
import javax.swing.SwingUtilities
import javax.swing.filechooser.FileNameExtensionFilter

@OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)
class SwingAppHost private constructor(
    val frame: JFrame,
    val gameCanvas: Canvas,
    val menuPanel: ComposePanel,
    private val shutdownRenderer: () -> Unit,
    skiaGameContent: (@Composable (inputEnabled: Boolean) -> Unit)? = null,
) : PlatformFilePickerHost {
    private val skiaOverlayContent = mutableStateOf<(@Composable () -> Unit)?>(null)
    private val skiaInputEnabled = mutableStateOf(false)
    private val skiaGamePanel: ComposePanel? = skiaGameContent?.let { game ->
        createSkiaGamePanel(isVsyncEnabled = SettingsEngine.getInstance().renderVsync).apply {
            setContent {
                Box(Modifier.fillMaxSize()) {
                    game(skiaInputEnabled.value)
                    skiaOverlayContent.value?.invoke()
                }
            }
        }
    }
    private val content = JLayeredPane()
    private val composeTexturePanel = ComposeTexturePanel(menuPanel)
    private val snapshotPanel = SlickSnapshotPanel()
    private var composeHost: DesktopComposeOverlay? = null
    private var session: AppSession? = null
    private val backInput = CanvasBackInput(
        gameCanvas,
        canHandleBack = { uiState?.screen != AppScreen.InGame },
    ) { if (!closing.get()) session?.navigateBack() }
    private var gameRequested = false
    private var overlayRequested = false
    private var uiState: AppUiState? = null
    private val closing = AtomicBoolean(false)
    private val closed = CompletableFuture<Unit>()
    val closeCompletion: java.util.concurrent.CompletionStage<Unit> get() = closed

    @Volatile
    var viewport: GameViewport = GameViewport(1280, 720)
        private set

    init {
        checkEdt()
        content.background = Color.BLACK
        content.isOpaque = true
        content.preferredSize = Dimension(1280, 720)
        gameCanvas.name = "rwx-game"
        gameCanvas.background = Color.BLACK
        gameCanvas.isFocusable = true
        gameCanvas.ignoreRepaint = true
        gameCanvas.isVisible = false
        content.add(gameCanvas, JLayeredPane.MODAL_LAYER, 0)
        skiaGamePanel?.let { content.add(it, JLayeredPane.DEFAULT_LAYER, 0) }
        content.add(snapshotPanel, 50, 0)
        content.add(composeTexturePanel, JLayeredPane.PALETTE_LAYER, 0)
        content.addComponentListener(object : ComponentAdapter() {
            override fun componentResized(e: ComponentEvent) = resizeContent()
        })
        frame.contentPane = content
        frame.minimumSize = Dimension(800, 600)
        frame.defaultCloseOperation = JFrame.DO_NOTHING_ON_CLOSE
        frame.addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent) { requestClose() }
        })
    }

    fun installComposeStartupUi(state: AppUiState) {
        checkEdt()
        if (closing.get() || composeHost != null) return
        uiState = state
        val mergedPanel = skiaGamePanel
        composeHost = if (mergedPanel != null) {
            DesktopComposeOverlay(
                initialState = state,
                hostFactory = { SkiaOverlayPanelHost(mergedPanel, skiaOverlayContent) },
                onStateChanged = { uiState = it; updateVisibility() },
            )
        } else {
            installComposeOverlay(
                frame, gameCanvas, menuPanel, initialState = state,
                onVisibilityChanged = { updateVisibility() },
                onStateChanged = { uiState = it; updateVisibility() },
            )
        }
    }

    fun installComposeUi(session: AppSession) {
        checkEdt()
        if (closing.get()) { session.close(); return }
        check(this.session == null) { "An app session is already attached" }
        if (composeHost == null) installComposeStartupUi(session.uiState.value)
        this.session = session
        composeHost!!.attach(session)
    }

    fun presentSnapshot(snapshot: SlickFrameSnapshot?) {
        checkEdt()
        if (!closing.get()) snapshotPanel.present(snapshot)
    }

    fun showGame(overlay: Boolean = false) {
        checkEdt()
        if (closing.get()) return
        gameRequested = true
        overlayRequested = overlay
        updateVisibility()
    }

    fun showMenu() {
        checkEdt()
        if (closing.get()) return
        gameRequested = false
        overlayRequested = false
        updateVisibility()
    }

    private fun updateVisibility() {
        checkEdt()
        if (closing.get()) return
        val mergedPanel = skiaGamePanel
        if (mergedPanel != null) {
            // Skia renders game and menu overlay in one GPU composition; the Slick
            // widgets stay parked. Overlay content gates itself on UI state.
            skiaInputEnabled.value = acceptsDirectSkiaInput(uiState)
            gameCanvas.isVisible = false
            menuPanel.isVisible = false
            composeTexturePanel.isVisible = false
            snapshotPanel.isVisible = false
            if (!mergedPanel.isVisible) {
                mergedPanel.isVisible = true
                content.revalidate()
                content.repaint()
                mergedPanel.requestFocusInWindow()
            }
            return
        }
        val state = uiState
        val showGame = gameRequested
        val showUi = !showGame || overlayRequested || state?.showComposeOverlay == true ||
            state?.inGameOverlay?.visible == true
        val changed = gameCanvas.isVisible != showGame || menuPanel.isVisible != showUi
        gameCanvas.isVisible = showGame
        menuPanel.isVisible = showUi
        composeTexturePanel.isVisible = showUi
        composeTexturePanel.nativePresentation = showGame && showUi
        snapshotPanel.isVisible = !showGame && true && (state == null ||
            state.battleBackgroundVisible || state.screen == AppScreen.Paused || state.screen == AppScreen.InGame)
        if (changed) {
            content.revalidate()
            content.repaint()
            if (showUi) menuPanel.requestFocusInWindow() else gameCanvas.requestFocusInWindow()
        }
    }

    private fun resizeContent() {
        checkEdt()
        val width = content.width.coerceAtLeast(1)
        val height = content.height.coerceAtLeast(1)
        gameCanvas.setBounds(0, 0, width, height)
        skiaGamePanel?.setBounds(0, 0, width, height)
        snapshotPanel.setBounds(0, 0, width, height)
        composeTexturePanel.setBounds(0, 0, width, height)
        composeTexturePanel.doLayout()
        val scale = gameCanvas.graphicsConfiguration?.defaultTransform
        viewport = GameViewport(
            (width * (scale?.scaleX ?: 1.0)).toInt().coerceAtLeast(1),
            (height * (scale?.scaleY ?: 1.0)).toInt().coerceAtLeast(1),
        )
        SlickCanvasHost.notifyGameCanvasResized(width, height)
    }

    fun requestClose() { dispose() }

    /** Never join Slick on EDT: it may itself be waiting for an EDT canvas operation. */
    fun dispose(): CompletableFuture<Unit> {
        if (!closing.compareAndSet(false, true)) return closed
        onEdt {
            try {
                session?.close()
                session = null
                Thread({
                    try {
                        shutdownRenderer() // Must return only after the render thread released JAWT.
                        check((gameCanvas as? SlickAwtGLCanvas)?.hasLiveDrawingSurface != true) {
                            "Renderer stopped without releasing its JAWT surface; refusing to dispose the frame"
                        }
                        SwingUtilities.invokeLater {
                            try {
                                backInput.close()
                                composeHost?.dispose()
                                composeHost = null
                                SlickCanvasHost.uninstall(gameCanvas)
                                skiaGamePanel?.dispose()
                                menuPanel.dispose()
                                frame.dispose()
                                closed.complete(Unit)
                            } catch (error: Throwable) { closed.completeExceptionally(error) }
                        }
                    } catch (error: Throwable) {
                        // A timeout/failure is not permission to free a live native drawing surface.
                        closed.completeExceptionally(error)
                    }
                }, "RWX-desktop-shutdown").start()
            } catch (error: Throwable) { closed.completeExceptionally(error) }
        }
        return closed
    }

    override fun openFilePicker(
        title: String,
        allowedExtensions: Set<String>,
        allowDirectories: Boolean,
        onResult: (PlatformFileSelection?) -> Unit,
    ) = onEdt {
        if (closing.get()) { onResult(null); return@onEdt }
        val selection = runCatching {
            val extensions = allowedExtensions.map { it.trim().removePrefix(".") }
                .filter(String::isNotEmpty).sorted().toTypedArray()
            val chooser = JFileChooser().apply {
                dialogTitle = title
                fileSelectionMode = if (allowDirectories) JFileChooser.FILES_AND_DIRECTORIES else JFileChooser.FILES_ONLY
                isMultiSelectionEnabled = false
                if (extensions.isNotEmpty()) {
                    fileFilter = FileNameExtensionFilter("Supported files (${extensions.joinToString()})", *extensions)
                    isAcceptAllFileFilterUsed = false
                }
            }
            chooser.takeIf { it.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION }
                ?.selectedFile?.absoluteFile?.let { PlatformFileSelection(path = it.path) }
        }.getOrNull()
        onResult(selection)
    }

    companion object {
        fun create(
            fullscreen: Boolean = false,
            shutdownRenderer: () -> Unit = SlickCanvasHost::shutdownRenderer,
            skiaGameContent: (@Composable (inputEnabled: Boolean) -> Unit)? = null,
        ): SwingAppHost {
            checkEdt() // Check before creating even the first Swing/Compose/native component.
            System.setProperty("org.lwjgl.opengl.contextAPI", "native")
            val frame = JFrame(System.getProperty("rwx.windowTitle")
                ?: System.getenv("RWX_WINDOW_TITLE") ?: "RWX Game").apply { isUndecorated = fullscreen }
            val canvas = SlickAwtGLCanvas(GLData().apply { alphaSize = 8; depthSize = 24; stencilSize = 8 }, 0)
            val host = SwingAppHost(
                frame, canvas, createDesktopComposePanel(), shutdownRenderer,
                skiaGameContent,
            )
            SlickCanvasHost.install({ host.gameCanvas }) { visible, overlay ->
                // Showing the initial peer must finish before Slick starts. Once showing, never
                // wait under runInContext's JAWT lock: dispatch visibility asynchronously instead.
                dispatchCanvasVisibilityChange(host.gameCanvas.isShowing) {
                    if (visible) host.showGame(overlay) else host.showMenu()
                }
            }
            SlickCanvasHost.installUiOverlay(
                frameProvider = { host.composeTexturePanel.uiFrame },
                inputDispatcher = host.composeTexturePanel::dispatchInput,
            )
            frame.pack()
            if (fullscreen) frame.bounds = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .defaultScreenDevice.defaultConfiguration.bounds else frame.setLocationRelativeTo(null)
            frame.isVisible = true
            host.resizeContent()
            host.showMenu()
            return host
        }

        private fun checkEdt() {
            check(SwingUtilities.isEventDispatchThread()) { "Swing app host operations must run on the EDT" }
        }
        private fun onEdt(action: () -> Unit) {
            if (SwingUtilities.isEventDispatchThread()) action() else SwingUtilities.invokeLater(action)
        }
    }
}

internal fun dispatchCanvasVisibilityChange(gameCanvasShowing: Boolean, action: () -> Unit) {
    if (SwingUtilities.isEventDispatchThread()) action()
    else if (gameCanvasShowing) SwingUtilities.invokeLater(action)
    else SwingUtilities.invokeAndWait(action)
}
