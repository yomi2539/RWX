package io.github.rwx

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import android.view.*
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.lifecycleScope
import io.github.rwx.app.*
import io.github.rwx.di.selectedAndroidRenderBackend
import io.github.rwx.p2p.P2PLobbyService
import io.github.rwx.session.GameSession
import io.github.rwx.settings.KEY_ANDROID_OPENGL_RENDERER
import io.github.rwx.ui.AndroidComposeOverlay
import io.github.rwx.ui.AppUiState
import io.github.rwx.ui.model.LoadingUiState
import io.github.rwx.ui.platform.createAndroidComposeHost
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.get
import org.koin.core.component.KoinComponent
import java.io.File
import java.util.*

class MainActivity : ComponentActivity(), PlatformFilePickerHost, KoinComponent {

    private var frameScheduler: AndroidFrameScheduler? = null
    private var appSession: AppSession? = null
    private var composeView: ComposeView? = null
    private var composeOverlay: AndroidComposeOverlay? = null
    private var nativeGameSession: AndroidGameSession? = null
    private var rendererPreferenceListener: SharedPreferences.OnSharedPreferenceChangeListener? = null
    private var pendingStoragePickerResult: ((ExternalStorageSelection?) -> Unit)? = null
    private var pendingFilePickerResult: ((PlatformFileSelection?) -> Unit)? = null
    private var applicationExitRequested: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bridge = get<PlatformBridge>()
        bridge.filePickerHost = this
        val selectedGameSession = get<GameSession>()
        val gameSession = requireNotNull(selectedGameSession as? AndroidGameSession) {
            "Android requires a native game session"
        }
        nativeGameSession = gameSession
        observeNativeRendererPreference()
        val root = FrameLayout(this)
        val composeView = ComposeView(this).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            visibility = View.GONE
        }
        composeOverlay = AndroidComposeOverlay(
            initialState = AppUiState(composeEnabled = true, loading = LoadingUiState()),
            hostFactory = { onDispose -> createAndroidComposeHost(composeView, onDispose) },
        )
        gameSession.attach(this, root, composeView)
        this.composeView = composeView
        setContentView(root)
        enterImmersiveMode()

        val scheduler = AndroidFrameScheduler(lifecycle)
        frameScheduler = scheduler
        val session = installApp(
            viewportProvider = gameSession::viewport,
            scheduler = scheduler,
            // updateFrame records and submits directly to the native Canvas/OpenGL presenter.
            presenter = GameFramePresenter { },
            options = AppOptions(isDesktop = false),
            onQuit = ::exitApplication,
        )
        appSession = session
        composeOverlay?.attach(session)
        launchOnIO(exceptionHandler = { _, error ->
            logger.warn(error) { "Unable to prewarm RWX P2P during startup" }
        }) {
            P2PLobbyService.getInstance().startIfNeeded()
        }
    }

    private fun exitApplication() {
        runOnUiThread {
            if (applicationExitRequested) return@runOnUiThread
            applicationExitRequested = true
            finishAndRemoveTask()
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            appSession?.navigateBack() ?: return super.onKeyDown(keyCode, event)
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onResume() {
        super.onResume()
        enterImmersiveMode()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            enterImmersiveMode()
        }
    }

    override fun onDestroy() {
        val terminateProcess = applicationExitRequested
        frameScheduler?.close()
        frameScheduler = null
        val bridge = get<PlatformBridge>()
        if (bridge.filePickerHost === this) bridge.filePickerHost = null
        pendingStoragePickerResult?.invoke(null)
        pendingStoragePickerResult = null
        pendingFilePickerResult?.invoke(null)
        pendingFilePickerResult = null
        composeOverlay?.dispose()
        composeOverlay = null
        composeView = null
        appSession?.close()
        appSession = null
        nativeGameSession?.detach()
        rendererPreferenceListener?.let { listener ->
            getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE)
                .unregisterOnSharedPreferenceChangeListener(listener)
        }
        rendererPreferenceListener = null
        nativeGameSession = null
        super.onDestroy()
        if (terminateProcess) {
            Process.killProcess(Process.myPid())
        }
    }

    private fun observeNativeRendererPreference() {
        if (nativeGameSession == null) return
        val preferences = getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE)
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
            if (key != KEY_ANDROID_OPENGL_RENDERER) return@OnSharedPreferenceChangeListener
            val backend = selectedAndroidRenderBackend(
                useOpenGlPreference = prefs.getBoolean(KEY_ANDROID_OPENGL_RENDERER, false),
                incompleteLoadAttempts = prefs.getInt("numIncompleteLoadAttempts", 0),
                loadsSinceNormalExit = prefs.getInt("numLoadsSinceRunningGameOrNormalExit", 0),
            )
            runOnUiThread {
                nativeGameSession?.switchRenderBackend(backend)
            }
        }
        preferences.registerOnSharedPreferenceChangeListener(listener)
        rendererPreferenceListener = listener
    }

    override fun requestExternalStorage(onResult: (ExternalStorageSelection?) -> Unit) {
        runOnUiThread {
            pendingStoragePickerResult?.invoke(null)
            pendingStoragePickerResult = onResult
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
                addFlags(
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                            Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION or
                            Intent.FLAG_GRANT_PREFIX_URI_PERMISSION
                )
            }
            startActivityForResult(intent, EXTERNAL_STORAGE_TREE_REQUEST_CODE)
        }
    }

    override fun openFilePicker(
        title: String,
        allowedExtensions: Set<String>,
        allowDirectories: Boolean,
        onResult: (PlatformFileSelection?) -> Unit,
    ) {
        runOnUiThread {
            pendingFilePickerResult?.invoke(null)
            pendingFilePickerResult = onResult
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*"
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivityForResult(Intent.createChooser(intent, title), FILE_PICKER_REQUEST_CODE)
        }
    }

    @Deprecated("Deprecated in Android API")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            EXTERNAL_STORAGE_TREE_REQUEST_CODE -> {
                val callback = pendingStoragePickerResult ?: return
                pendingStoragePickerResult = null
                val uri = data?.data?.takeIf { resultCode == RESULT_OK }
                val selection = uri?.let(::persistExternalStorageSelection)
                callback(selection)
            }

            FILE_PICKER_REQUEST_CODE -> {
                val callback = pendingFilePickerResult ?: return
                pendingFilePickerResult = null
                val uri = data?.data?.takeIf { resultCode == RESULT_OK }
                lifecycleScope.launch {
                    val selection = uri?.let {
                        withContext(Dispatchers.IO) { stageSelectedFile(it) }
                    }
                    callback(selection)
                }
            }
        }
    }

    private fun persistExternalStorageSelection(uri: Uri): ExternalStorageSelection? = runCatching {
        contentResolver.takePersistableUriPermission(
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
        )
        ExternalStorageSelection(
            uri = uri.toString(),
            displayPath = DocumentsContract.getTreeDocumentId(uri),
        )
    }.getOrNull()

    private fun stageSelectedFile(uri: Uri): PlatformFileSelection? {
        var stagingDirectory: File? = null
        return runCatching {
            val displayPath = documentDisplayName(uri) ?: "selected-file"
            val safeName = File(displayPath).name
                .takeIf { it.isNotBlank() && it != "." && it != ".." }
                ?: "selected-file"
            val cacheRoot = File(cacheDir, FILE_PICKER_CACHE_DIRECTORY).apply {
                check(isDirectory || mkdirs()) { "Unable to create file picker cache" }
            }
            val selectionDirectory = File(cacheRoot, UUID.randomUUID().toString()).apply {
                check(mkdirs()) { "Unable to create selection cache" }
            }
            stagingDirectory = selectionDirectory
            val selectedFile = File(selectionDirectory, safeName)
            contentResolver.openInputStream(uri)?.buffered().use { input ->
                checkNotNull(input) { "Unable to open selected file" }
                selectedFile.outputStream().buffered().use { output -> input.copyTo(output) }
            }
            PlatformFileSelection(
                path = selectedFile.absolutePath,
                displayPath = displayPath,
                release = { selectionDirectory.deleteRecursively() },
            )
        }.onFailure {
            stagingDirectory?.deleteRecursively()
        }.getOrNull()
    }

    private fun documentDisplayName(uri: Uri): String? =
        contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            val displayNameColumn = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (displayNameColumn >= 0 && cursor.moveToFirst()) cursor.getString(displayNameColumn) else null
        }

    private fun enterImmersiveMode() {
        window.attributes = window.attributes.apply {
            layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.decorView.windowInsetsController?.apply {
                systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                        View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        }
    }
}

private const val EXTERNAL_STORAGE_TREE_REQUEST_CODE: Int = 9124
private const val FILE_PICKER_REQUEST_CODE: Int = 9125
private const val FILE_PICKER_CACHE_DIRECTORY: String = "file-picker"
