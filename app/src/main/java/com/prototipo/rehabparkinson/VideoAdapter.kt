package com.prototipo.rehabparkinson

import android.media.MediaMetadataRetriever
import android.media.ThumbnailUtils
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class VideoAdapter(
    private val onClick: (File) -> Unit
) : RecyclerView.Adapter<VideoAdapter.VideoViewHolder>() {

    private var videos: List<File> = emptyList()

    fun updateList(list: List<File>) {
        videos = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_video, parent, false)
        return VideoViewHolder(view)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        val file = videos[position]
        holder.bind(file)
    }

    override fun getItemCount(): Int = videos.size

    inner class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgThumbnail: ImageView = itemView.findViewById(R.id.imgThumbnail)
        private val tvName: TextView = itemView.findViewById(R.id.tvVideoName)
        private val tvDuration: TextView = itemView.findViewById(R.id.tvVideoDuration)

        fun bind(file: File) {
            val rawName = file.name.removeSuffix(".mp4")
            val parts = rawName.split("_")

            // Código de ejercicio (ej. EC05, CC01…)
            val codigo = parts.getOrNull(0) ?: "???"

            // Diccionario de ejercicios
            val mapaEjercicios = mapOf(
                "EC05" to "Elevación brazo der. + separación brazo izq.",
                "EC03" to "Mano al oído contralateral",
                "CL01" to "Flexión lateral del tronco",
                "CC02" to "Inclinación lateral derecha",
                "CC01" to "Inclinación lateral izquierda"
            )

            val descripcion = mapaEjercicios[codigo] ?: "Ejercicio $codigo"

            // Fecha si está en el nombre (ej. EC05_280825_1234.mp4)
            val fechaFormateada = try {
                val fechaStr = parts.getOrNull(1) ?: ""
                val sdfEntrada = SimpleDateFormat("ddMMyy", Locale.getDefault())
                val sdfSalida = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                sdfSalida.format(sdfEntrada.parse(fechaStr)!!)
            } catch (e: Exception) {
                ""
            }

            val nombreBonito = if (fechaFormateada.isNotEmpty()) {
                "$descripcion - $fechaFormateada"
            } else {
                descripcion
            }

            tvName.text = nombreBonito

            // Miniatura
            val bitmap = ThumbnailUtils.createVideoThumbnail(
                file.absolutePath,
                MediaStore.Images.Thumbnails.MINI_KIND
            )
            if (bitmap != null) {
                imgThumbnail.setImageBitmap(bitmap)
            } else {
                imgThumbnail.setImageResource(R.drawable.ic_video_placeholder)
            }

            // Duración
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(file.absolutePath)
            val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            retriever.release()

            val durationMs = durationStr?.toLongOrNull() ?: 0
            val minutes = (durationMs / 1000) / 60
            val seconds = (durationMs / 1000) % 60
            tvDuration.text = String.format("%02d:%02d", minutes, seconds)

            // Acción click
            itemView.setOnClickListener { onClick(file) }
        }
    }
}
