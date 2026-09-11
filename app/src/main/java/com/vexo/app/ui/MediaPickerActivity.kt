package com.vexo.app.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vexo.app.databinding.ActivityMediaPickerBinding
import com.vexo.app.databinding.ItemMediaBinding

class MediaPickerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMediaPickerBinding
    private val selectedUris = ArrayList<Uri>()
    private val allMedia = mutableListOf<Uri>()
    private lateinit var adapter: MediaAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMediaPickerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        loadMedia()

        binding.ivBack.setOnClickListener { finish() }

        binding.btnAdd.setOnClickListener {
            if (selectedUris.isEmpty()) {
                Toast.makeText(this, "Select at least one media", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val result = Intent()
            result.putParcelableArrayListExtra("selected_media", selectedUris)
            setResult(Activity.RESULT_OK, result)
            finish()
        }

        binding.btnSelectAll.setOnClickListener {
            if (selectedUris.size == allMedia.size) {
                selectedUris.clear()
            } else {
                selectedUris.clear()
                selectedUris.addAll(allMedia)
            }
            adapter.notifyDataSetChanged()
            updateAddButton()
        }
    }

    private fun setupRecyclerView() {
        adapter = MediaAdapter(allMedia, selectedUris) {
            updateAddButton()
        }
        binding.rvMedia.layoutManager = GridLayoutManager(this, 3)
        binding.rvMedia.adapter = adapter
    }

    private fun loadMedia() {
        val projection = arrayOf(MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.MEDIA_TYPE,
            MediaStore.Files.FileColumns.DATE_MODIFIED)
        val selection = (MediaStore.Files.FileColumns.MEDIA_TYPE + "=" +
                MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE + " OR " +
                MediaStore.Files.FileColumns.MEDIA_TYPE + "=" +
                MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO)
        val cursor = contentResolver.query(
            MediaStore.Files.getContentUri("external"),
            projection, selection, null,
            MediaStore.Files.FileColumns.DATE_MODIFIED + " DESC"
        )
        cursor?.use {
            val idCol = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
            val typeCol = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE)
            while (it.moveToNext()) {
                val id = it.getLong(idCol)
                val type = it.getInt(typeCol)
                val uri = if (type == MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE) {
                    Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id.toString())
                } else {
                    Uri.withAppendedPath(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id.toString())
                }
                allMedia.add(uri)
            }
        }
        adapter.notifyDataSetChanged()
    }

    private fun updateAddButton() {
        val count = selectedUris.size
        binding.btnAdd.text = if (count > 0) "Add ($count)" else "Add"
    }

    inner class MediaAdapter(
        private val items: List<Uri>,
        private val selected: ArrayList<Uri>,
        private val onSelectionChanged: () -> Unit
    ) : RecyclerView.Adapter<MediaAdapter.VH>() {

        inner class VH(val binding: ItemMediaBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            return VH(ItemMediaBinding.inflate(
                LayoutInflater.from(parent.context), parent, false))
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val uri = items[position]
            Glide.with(holder.itemView.context)
                .load(uri)
                .centerCrop()
                .into(holder.binding.ivThumbnail)

            val isSelected = selected.contains(uri)
            holder.binding.viewSelected.alpha = if (isSelected) 1f else 0f
            holder.binding.tvOrder.text = if (isSelected)
                (selected.indexOf(uri) + 1).toString() else ""
            holder.binding.tvOrder.alpha = if (isSelected) 1f else 0f

            holder.itemView.setOnClickListener {
                if (selected.contains(uri)) {
                    selected.remove(uri)
                } else {
                    selected.add(uri)
                }
                notifyDataSetChanged()
                onSelectionChanged()
            }
        }

        override fun getItemCount() = items.size
    }
}
