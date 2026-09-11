package com.vexo.app.ui

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.net.Uri
import android.os.Bundle
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.vexo.app.databinding.ActivityPhotoEditorBinding

class PhotoEditorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPhotoEditorBinding
    private var photoUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhotoEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        photoUri = intent.getParcelableExtra("photo_uri")
        photoUri?.let {
            Glide.with(this).load(it).into(binding.ivPhoto)
        }

        binding.ivBack.setOnClickListener { finish() }

        setupAdjustTools()
        setupFilterTools()
    }

    private fun setupAdjustTools() {
        binding.seekBrightness.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                val brightness = (progress - 50) / 50f
                val matrix = ColorMatrix().apply {
                    set(floatArrayOf(
                        1f, 0f, 0f, 0f, brightness * 255,
                        0f, 1f, 0f, 0f, brightness * 255,
                        0f, 0f, 1f, 0f, brightness * 255,
                        0f, 0f, 0f, 1f, 0f
                    ))
                }
                binding.ivPhoto.colorFilter = ColorMatrixColorFilter(matrix)
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })

        binding.seekSaturation.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                val sat = progress / 50f
                val matrix = ColorMatrix().apply { setSaturation(sat) }
                binding.ivPhoto.colorFilter = ColorMatrixColorFilter(matrix)
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })

        binding.seekContrast.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                val contrast = progress / 50f
                val translate = (-.5f * contrast + .5f) * 255f
                val matrix = ColorMatrix().apply {
                    set(floatArrayOf(
                        contrast, 0f, 0f, 0f, translate,
                        0f, contrast, 0f, 0f, translate,
                        0f, 0f, contrast, 0f, translate,
                        0f, 0f, 0f, 1f, 0f
                    ))
                }
                binding.ivPhoto.colorFilter = ColorMatrixColorFilter(matrix)
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })

        binding.btnReset.setOnClickListener {
            binding.ivPhoto.clearColorFilter()
            binding.seekBrightness.progress = 50
            binding.seekSaturation.progress = 50
            binding.seekContrast.progress = 50
        }
    }

    private fun setupFilterTools() {
        binding.btnFilterNone.setOnClickListener {
            binding.ivPhoto.clearColorFilter()
        }
        binding.btnFilterBW.setOnClickListener {
            val matrix = ColorMatrix().apply { setSaturation(0f) }
            binding.ivPhoto.colorFilter = ColorMatrixColorFilter(matrix)
        }
        binding.btnFilterWarm.setOnClickListener {
            val matrix = ColorMatrix().apply {
                set(floatArrayOf(
                    1.2f, 0f, 0f, 0f, 20f,
                    0f, 1f, 0f, 0f, 0f,
                    0f, 0f, 0.8f, 0f, -20f,
                    0f, 0f, 0f, 1f, 0f
                ))
            }
            binding.ivPhoto.colorFilter = ColorMatrixColorFilter(matrix)
        }
        binding.btnFilterCool.setOnClickListener {
            val matrix = ColorMatrix().apply {
                set(floatArrayOf(
                    0.8f, 0f, 0f, 0f, -20f,
                    0f, 1f, 0f, 0f, 0f,
                    0f, 0f, 1.2f, 0f, 20f,
                    0f, 0f, 0f, 1f, 0f
                ))
            }
            binding.ivPhoto.colorFilter = ColorMatrixColorFilter(matrix)
        }
        binding.btnFilterVintage.setOnClickListener {
            val matrix = ColorMatrix().apply {
                set(floatArrayOf(
                    0.9f, 0.1f, 0f, 0f, 20f,
                    0.1f, 0.8f, 0.1f, 0f, 10f,
                    0f, 0.1f, 0.7f, 0f, -10f,
                    0f, 0f, 0f, 1f, 0f
                ))
            }
            binding.ivPhoto.colorFilter = ColorMatrixColorFilter(matrix)
        }
        binding.btnSave.setOnClickListener {
            Toast.makeText(this, "✅ Photo Saved!", Toast.LENGTH_SHORT).show()
        }
    }
}
