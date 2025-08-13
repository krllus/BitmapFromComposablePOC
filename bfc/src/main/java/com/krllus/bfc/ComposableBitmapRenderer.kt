package com.krllus.bfc

import android.app.Application
import android.app.Presentation
import android.content.Context
import android.content.Context.DISPLAY_SERVICE
import android.graphics.Bitmap
import android.graphics.SurfaceTexture
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.EGLContext
import android.opengl.EGLDisplay
import android.opengl.EGLSurface
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.view.Display
import android.view.Surface
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine

interface ComposableBitmapRenderer {

    suspend fun renderComposableToBitmap(
        canvasSize: Size,
        composableContent: @Composable () -> Unit
    ): Bitmap?
}

/**
 * Use a virtual display to capture composable content thats on a display.
 * This is necessary because Compose doesn't yet support offscreen bitmap creation (https://issuetracker.google.com/298037598)
 *
 * Original source: https://gist.github.com/iamcalledrob/871568679ad58e64959b097d4ef30738
 * Adapted to use new GraphicsLayer commands (record and toBitmap())
 *     Usage example:
 *     val offscreenBitmapManager = OffscreenBitmapManager(context)
 *     val bitmap = offscreenBitmapManager.renderComposableToBitmap {
 *              ImageResult() // etc
 *              }
 */

// https://github.com/android/androidify

class ComposableBitmapRendererImpl (
    private val application: Application
) : ComposableBitmapRenderer {

    private suspend fun <T> useVirtualDisplay(callback: suspend (display: Display) -> T): T? {
        var eglDisplay: EGLDisplay? = null
        var eglContext: EGLContext? = null
        var eglSurface: EGLSurface? = null
        var surfaceTexture: SurfaceTexture? = null
        var surface: Surface? = null
        var virtualDisplay: VirtualDisplay? = null
        var texName = -1 // Initialize with an invalid value

        try {
            eglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
            if (eglDisplay == EGL14.EGL_NO_DISPLAY) {
                throw RuntimeException("eglGetDisplay failed")
            }
            val version = IntArray(2)
            if (!EGL14.eglInitialize(eglDisplay, version, 0, version, 1)) {
                throw RuntimeException("eglInitialize failed")
            }

            val attribList = intArrayOf(
                EGL14.EGL_RED_SIZE, 8,
                EGL14.EGL_GREEN_SIZE, 8,
                EGL14.EGL_BLUE_SIZE, 8,
                EGL14.EGL_ALPHA_SIZE, 8,
                EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                EGL14.EGL_NONE
            )
            val configs: Array<EGLConfig?> = arrayOfNulls(1)
            val numConfigs = IntArray(1)
            if (!EGL14.eglChooseConfig(eglDisplay, attribList, 0, configs, 0, configs.size, numConfigs, 0)) {
                throw RuntimeException("eglChooseConfig failed")
            }
            val eglConfig = configs[0]

            val contextAttribs = intArrayOf(
                EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
                EGL14.EGL_NONE
            )
            eglContext = EGL14.eglCreateContext(eglDisplay, eglConfig, EGL14.EGL_NO_CONTEXT, contextAttribs, 0)
            if (eglContext == EGL14.EGL_NO_CONTEXT) {
                throw RuntimeException("eglCreateContext failed")
            }

            val surfaceAttribs = intArrayOf(EGL14.EGL_WIDTH, 1, EGL14.EGL_HEIGHT, 1, EGL14.EGL_NONE)
            eglSurface = EGL14.eglCreatePbufferSurface(eglDisplay, eglConfig, surfaceAttribs, 0)
            EGL14.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)

            val textures = IntArray(1)
            GLES20.glGenTextures(1, textures, 0)
            texName = textures[0]
            if (texName <= 0) { // Check if glGenTextures actually returned a valid ID
                throw RuntimeException("Failed to generate texture ID.")
            }

            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texName)
            GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST)
            GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST)
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0) // Unbind

            surfaceTexture = SurfaceTexture(texName, false) // Using the generated texName
            surface = Surface(surfaceTexture)
            virtualDisplay = (application.getSystemService(DISPLAY_SERVICE) as DisplayManager).createVirtualDisplay(
                "virtualDisplay",
                1,
                1,
                72,
                surface,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY,
            )

            val result = callback(virtualDisplay!!.display)
            return result

        } catch (e: Exception) {
            e.printStackTrace()
            return null
        } finally {
            virtualDisplay?.release()
            surface?.release()
            surfaceTexture?.release()

            if (texName > 0 && eglDisplay != null && EGL14.eglGetCurrentContext() == eglContext) {
                GLES20.glDeleteTextures(1, intArrayOf(texName), 0)
            }

            eglDisplay?.let {
                if (EGL14.eglGetCurrentContext() == eglContext) {
                    EGL14.eglMakeCurrent(it, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT)
                }
                eglSurface?.let { surf -> EGL14.eglDestroySurface(it, surf) }
                eglContext?.let { ctx -> EGL14.eglDestroyContext(it, ctx) }
                EGL14.eglTerminate(it)
            }
        }
    }

    override suspend fun renderComposableToBitmap(canvasSize: Size, composableContent: @Composable () -> Unit): Bitmap? {
        val bitmap = useVirtualDisplay { display ->
            val outputDensity = Density(1f)

            val logicalHeightDp = canvasSize.height.dp
            val logicalWidthDp = canvasSize.width.dp

            val captureDpSize = DpSize(width = logicalWidthDp, height = logicalHeightDp)

            captureComposable(
                context = application.applicationContext,
                size = captureDpSize,
                density = outputDensity,
                display = display,
            ) {
                LaunchedEffect(Unit) {
                    capture()
                }
                composableContent()
            }
        }
        return bitmap
    }

    private data class CaptureComposableScope(val capture: () -> Unit)

    private fun Size.roundedToIntSize(): IntSize =
        IntSize(width.toInt(), height.toInt())

    private class EmptySavedStateRegistryOwner : SavedStateRegistryOwner {
        private val controller = SavedStateRegistryController.create(this).apply {
            performRestore(null)
        }

        private val lifecycleOwner: LifecycleOwner = ProcessLifecycleOwner.get()

        override val lifecycle: Lifecycle
            get() =
                object : Lifecycle() {
                    @Suppress("UNNECESSARY_SAFE_CALL")
                    override fun addObserver(observer: LifecycleObserver) {
                        lifecycleOwner?.lifecycle?.addObserver(observer)
                    }

                    @Suppress("UNNECESSARY_SAFE_CALL")
                    override fun removeObserver(observer: LifecycleObserver) {
                        lifecycleOwner?.lifecycle?.removeObserver(observer)
                    }

                    override val currentState = State.INITIALIZED
                }

        override val savedStateRegistry: SavedStateRegistry
            get() = controller.savedStateRegistry
    }

    /** Captures composable content, by default using a hidden window on the default display.
     *
     *  Be sure to invoke capture() within the composable content (e.g. in a LaunchedEffect) to perform the capture.
     *  This gives some level of control over when the capture occurs, so it's possible to wait for async resources */
    private suspend fun captureComposable(
        context: Context,
        size: DpSize,
        density: Density = Density(density = 1f),
        display: Display = (context.getSystemService(DISPLAY_SERVICE) as DisplayManager)
            .getDisplay(Display.DEFAULT_DISPLAY),
        content: @Composable CaptureComposableScope.() -> Unit,
    ): Bitmap {
        val presentation = Presentation(context.applicationContext, display).apply {
            window?.decorView?.let { view ->
                view.setViewTreeLifecycleOwner(ProcessLifecycleOwner.get())
                view.setViewTreeSavedStateRegistryOwner(EmptySavedStateRegistryOwner())
                view.alpha =
                    0f // If using default display, to ensure this does not appear on top of content.
            }
        }

        val composeView = ComposeView(context).apply {
            val intSize = with(density) { size.toSize().roundedToIntSize() }
            require(intSize.width > 0 && intSize.height > 0) { "pixel size must not have zero dimension" }

            layoutParams = ViewGroup.LayoutParams(intSize.width, intSize.height)
        }

        presentation.setContentView(composeView, composeView.layoutParams)
        presentation.show()

        val androidBitmap = suspendCancellableCoroutine { continuation ->
            composeView.setContent {
                val coroutineScope = rememberCoroutineScope()
                val graphicsLayer = rememberGraphicsLayer()
                Box(
                    modifier = Modifier
                        .size(size)
                        .drawWithContent {
                            graphicsLayer.record {
                                this@drawWithContent.drawContent()
                            }
                            drawLayer(graphicsLayer)
                        },
                ) {
                    CaptureComposableScope(
                        capture = {
                            coroutineScope.launch {
                                val composeImageBitmap = graphicsLayer.toImageBitmap()
                                continuation.resumeWith(Result.success(composeImageBitmap.asAndroidBitmap()))
                            }
                        },
                    ).content()
                }
            }
        }
        presentation.dismiss()
        return androidBitmap
    }
}