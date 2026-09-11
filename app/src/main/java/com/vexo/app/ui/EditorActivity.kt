package com.vexo.app.ui

import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import com.vexo.app.databinding.ActivityEditorBinding
import java.io.File

class EditorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditorBinding
    private var player: ExoPlayer? = null
    private var mediaUris = ArrayList<Uri>()
    private var currentUri: Uri? = null
    private var isMuted = false
    private var currentSpeed = 1.0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mediaUris = intent.getParcelableArrayListExtra("media_uris") ?: ArrayList()
        if (mediaUris.isNotEmpty()) {
            currentUri = mediaUris[0]
            setupPlayer()
        }

        setupToolbar()
        setupPlaybackControls()
        setupEditingTools()
        setupBottomToolbar()
    }

    private fun setupPlayer() {
        player = ExoPlayer.Builder(this).build()
        binding.playerView.player = player
        currentUri?.let {
            player?.setMediaItem(MediaItem.fromUri(it))
            player?.prepare()
            player?.playWhenReady = true
        }
    }

    private fun setupToolbar() {
        binding.ivClose.setOnClickListener { finish() }
        binding.btnExport.setOnClickListener { showExportDialog() }
        binding.ivUndo.setOnClickListener {
            Toast.makeText(this, "Undo", Toast.LENGTH_SHORT).show()
        }
        binding.ivRedo.setOnClickListener {
            Toast.makeText(this, "Redo", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupPlaybackControls() {
        binding.ivPlay.setOnClickListener {
            if (player?.isPlaying == true) {
                player?.pause()
                binding.ivPlay.setImageResource(android.R.drawable.ic_media_play)
            } else {
                player?.play()
                binding.ivPlay.setImageResource(android.R.drawable.ic_media_pause)
            }
        }

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val duration = player?.duration ?: 0L
                    player?.seekTo((progress * duration / 100))
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun setupEditingTools() {
        // Trim
        binding.toolTrim.setOnClickListener {
            showTrimPanel()
        }
        // Split
        binding.toolSplit.setOnClickListener {
            val pos = player?.currentPosition ?: 0L
            Toast.makeText(this, "Split at ${pos/1000}s", Toast.LENGTH_SHORT).show()
        }
        // Speed
        binding.toolSpeed.setOnClickListener {
            showSpeedPanel()
        }
        // Volume
        binding.toolVolume.setOnClickListener {
            showVolumePanel()
        }
        // Rotate
        binding.toolRotate.setOnClickListener {
            applyFFmpegCommand("rotate")
        }
        // Flip
        binding.toolFlip.setOnClickListener {
            applyFFmpegCommand("flip")
        }
        // Crop
        binding.toolCrop.setOnClickListener {
            Toast.makeText(this, "Crop tool", Toast.LENGTH_SHORT).show()
        }
        // Duplicate
        binding.toolDuplicate.setOnClickListener {
            currentUri?.let { mediaUris.add(it) }
            Toast.makeText(this, "Clip duplicated", Toast.LENGTH_SHORT).show()
        }
        // Delete
        binding.toolDelete.setOnClickListener {
            if (mediaUris.isNotEmpty()) {
                mediaUris.removeAt(0)
                Toast.makeText(this, "Clip deleted", Toast.LENGTH_SHORT).show()
            }
        }
        // Reverse
        binding.toolReverse.setOnClickListener {
            applyFFmpegCommand("reverse")
        }
        // Mute
        binding.toolMute.setOnClickListener {
            isMuted = !isMuted
            player?.volume = if (isMuted) 0f else 1f
            binding.toolMute.alpha = if (isMuted) 0.5f else 1f
            Toast.makeText(this,
                if (isMuted) "Muted" else "Unmuted",
                Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupBottomToolbar() {
        binding.tabAudio.setOnClickListener { showPanel("audio") }
        binding.tabText.setOnClickListener { showPanel("text") }
        binding.tabFilters.setOnClickListener { showPanel("filters") }
        binding.tabEffects.setOnClickListener { showPanel("effects") }
        binding.tabAdjust.setOnClickListener { showPanel("adjust") }
        binding.tabTransitions.setOnClickListener { showPanel("transitions") }
        binding.tabOverlay.setOnClickListener { showPanel("overlay") }
        binding.tabCanvas.setOnClickListener { showPanel("canvas") }
        binding.tabStickers.setOnClickListener { showPanel("stickers") }
        binding.tabCaptions.setOnClickListener { showPanel("captions") }
    }

    private fun showPanel(type: String) {
        binding.panelContainer.visibility = View.VISIBLE
        binding.tvPanelTitle.text = type.uppercase()
        when (type) {
            "filters" -> showFiltersPanel()
            "adjust" -> showAdjustPanel()
            "text" -> showTextPanel()
            "audio" -> showAudioPanel()
            else -> {
                binding.panelContainer.visibility = View.GONE
                Toast.makeText(this, "${type.capitalize()} — Coming Soon",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showFiltersPanel() {
        binding.filterStrip.visibility = View.VISIBLE
        binding.adjustPanel.visibility = View.GONE
        binding.textPanel.visibility = View.GONE
        binding.audioPanel.visibility = View.GONE
    }

    private fun showAdjustPanel() {
        binding.filterStrip.visibility = View.GONE
        binding.adjustPanel.visibility = View.VISIBLE
        binding.textPanel.visibility = View.GONE
        binding.audioPanel.visibility = View.GONE

        binding.seekBrightness.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, p: Int, fromUser: Boolean) {
                // Apply brightness via shader/effect
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })
    }

    private fun showTextPanel() {
        binding.filterStrip.visibility = View.GONE
        binding.adjustPanel.visibility = View.GONE
        binding.textPanel.visibility = View.VISIBLE
        binding.audioPanel.visibility = View.GONE
    }

    private fun showAudioPanel() {
        binding.filterStrip.visibility = View.GONE
        binding.adjustPanel.visibility = View.GONE
        binding.textPanel.visibility = View.GONE
        binding.audioPanel.visibility = View.VISIBLE
    }

    private fun showTrimPanel() {
        binding.trimPanel.visibility = View.VISIBLE
        binding.btnTrimDone.setOnClickListener {
            val startMs = binding.trimStart.progress * 1000L
            val endMs = binding.trimEnd.progress * 1000L
            applyTrim(startMs, endMs)
            binding.trimPanel.visibility = View.GONE
        }
        binding.btnTrimCancel.setOnClickListener {
            binding.trimPanel.visibility = View.GONE
        }
    }

    private fun showSpeedPanel() {
        binding.speedPanel.visibility = View.VISIBLE
        binding.btnSpeed025.setOnClickListener { setSpeed(0.25f) }
        binding.btnSpeed05.setOnClickListener { setSpeed(0.5f) }
        binding.btnSpeed1.setOnClickListener { setSpeed(1.0f) }
        binding.btnSpeed15.setOnClickListener { setSpeed(1.5f) }
        binding.btnSpeed2.setOnClickListener { setSpeed(2.0f) }
        binding.btnSpeed3.setOnClickListener { setSpeed(3.0f) }
        binding.btnSpeedDone.setOnClickListener {
            binding.speedPanel.visibility = View.GONE
        }
    }

    private fun showVolumePanel() {
        binding.volumePanel.visibility = View.VISIBLE
        binding.seekVolume.progress = 100
        binding.seekVolume.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                player?.volume = progress / 100f
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })
        binding.btnVolumeDone.setOnClickListener {
            binding.volumePanel.visibility = View.GONE
        }
    }

    private fun setSpeed(speed: Float) {
        currentSpeed = speed
        val params = player?.playbackParameters
        player?.playbackParameters = androidx.media3.common.PlaybackParameters(speed)
        Toast.makeText(this, "${speed}x", Toast.LENGTH_SHORT).show()
    }

    private fun applyTrim(startMs: Long, endMs: Long) {
        val inputPath = getRealPath(currentUri!!) ?: return
        val outputFile = File(cacheDir, "trimmed_${System.currentTimeMillis()}.mp4")
        val startSec = startMs / 1000.0
        val durationSec = (endMs - startMs) / 1000.0
        val cmd = "-i \"$inputPath\" -ss $startSec -t $durationSec -c copy \"${outputFile.absolutePath}\""
        showProgress(true)
        FFmpegKit.executeAsync(cmd) { session ->
            runOnUiThread {
                showProgress(false)
                if (ReturnCode.isSuccess(session.returnCode)) {
                    currentUri = Uri.fromFile(outputFile)
                    player?.setMediaItem(MediaItem.fromUri(currentUri!!))
                    player?.prepare()
                    Toast.makeText(this, "Trim applied!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Trim failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun applyFFmpegCommand(type: String) {
        val inputPath = getRealPath(currentUri!!) ?: return
        val outputFile = File(cacheDir, "${type}_${System.currentTimeMillis()}.mp4")
        val cmd = when (type) {
            "rotate" -> "-i \"$inputPath\" -vf \"transpose=1\" \"${outputFile.absolutePath}\""
            "flip" -> "-i \"$inputPath\" -vf \"hflip\" \"${outputFile.absolutePath}\""
            "reverse" -> "-i \"$inputPath\" -vf reverse -af areverse \"${outputFile.absolutePath}\""
            else -> return
        }
        showProgress(true)
        FFmpegKit.executeAsync(cmd) { session ->
            runOnUiThread {
                showProgress(false)
                if (ReturnCode.isSuccess(session.returnCode)) {
                    currentUri = Uri.fromFile(outputFile)
                    player?.setMediaItem(MediaItem.fromUri(currentUri!!))
                    player?.prepare()
                    Toast.makeText(this, "$type applied!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "$type failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showExportDialog() {
        binding.exportDialog.visibility = View.VISIBLE
        binding.btnExport480.setOnClickListener { exportVideo("854x480") }
        binding.btnExport720.setOnClickListener { exportVideo("1280x720") }
        binding.btnExport1080.setOnClickListener { exportVideo("1920x1080") }
        binding.btnExportCancel.setOnClickListener {
            binding.exportDialog.visibility = View.GONE
        }
    }

    private fun exportVideo(resolution: String) {
        binding.exportDialog.visibility = View.GONE
        val inputPath = getRealPath(currentUri!!) ?: return
        val outputFile = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
            "VEXO_${System.currentTimeMillis()}.mp4"
        )
        val cmd = "-i \"$inputPath\" -vf scale=$resolution -c:a aac \"${outputFile.absolutePath}\""
        showProgress(true)
        binding.tvExportProgress.text = "Exporting..."
        FFmpegKit.executeAsync(cmd) { session ->
            runOnUiThread {
                showProgress(false)
                if (ReturnCode.isSuccess(session.returnCode)) {
                    saveToGallery(outputFile)
                    Toast.makeText(this,
                        "✅ Export Complete! Saved to Gallery",
                        Toast.LENGTH_LONG).show()
                    showShareOption(outputFile)
                } else {
                    Toast.makeText(this, "Export failed. Try again.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun saveToGallery(file: File) {
        val values = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, file.name)
            put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES)
        }
        contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
    }

    private fun showShareOption(file: File) {
        binding.shareBar.visibility = View.VISIBLE
        binding.btnShareExported.setOnClickListener {
            val uri = Uri.fromFile(file)
            val share = Intent(Intent.ACTION_SEND).apply {
                type = "video/mp4"
                putExtra(Intent.EXTRA_STREAM, uri)
            }
            startActivity(Intent.createChooser(share, "Share via"))
        }
    }

    private fun showProgress(show: Boolean) {
        binding.progressOverlay.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun getRealPath(uri: Uri): String? {
        val projection = arrayOf(MediaStore.Video.Media.DATA)
        val cursor = contentResolver.query(uri, projection, null, null, null)
        return if (cursor != null) {
            cursor.moveToFirst()
            val path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA))
            cursor.close()
            path
        } else {
            uri.path
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
        player = null
    }
}
