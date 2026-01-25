package com.ximenez.joel.cazarpatos

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RankingAdapter(private val dataSet: ArrayList<Player>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_HEADER = 0
    private val TYPE_ITEM = 1

    class ViewHolderHeader(view: View) : RecyclerView.ViewHolder(view) {
        val textViewPosicion: TextView = view.findViewById(R.id.textViewPosicion)
        val textViewPatosCazados: TextView = view.findViewById(R.id.textViewPatosCazados)
        val textViewUsuario: TextView = view.findViewById(R.id.textViewUsuario)
    }

    class ViewHolderItem(view: View) : RecyclerView.ViewHolder(view) {
        val textViewPosicion: TextView = view.findViewById(R.id.textViewPosicion)
        val textViewPatosCazados: TextView = view.findViewById(R.id.textViewPatosCazados)
        val textViewUsuario: TextView = view.findViewById(R.id.textViewUsuario)
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) TYPE_HEADER else TYPE_ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.ranking_list, parent, false)

        return if (viewType == TYPE_HEADER) {
            ViewHolderHeader(view)
        } else {
            ViewHolderItem(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ViewHolderHeader) {
            // Header
            holder.textViewPosicion.text = "#"
            holder.textViewPatosCazados.text = "Patos"
            holder.textViewUsuario.text = "Usuario"

            // Subrayado
            holder.textViewPosicion.paintFlags =
                holder.textViewPosicion.paintFlags or Paint.UNDERLINE_TEXT_FLAG
            holder.textViewPatosCazados.paintFlags =
                holder.textViewPatosCazados.paintFlags or Paint.UNDERLINE_TEXT_FLAG
            holder.textViewUsuario.paintFlags =
                holder.textViewUsuario.paintFlags or Paint.UNDERLINE_TEXT_FLAG

            // Color
            val color = holder.itemView.context.getColor(R.color.colorPrimaryDark)
            holder.textViewPosicion.setTextColor(color)
            holder.textViewPatosCazados.setTextColor(color)
            holder.textViewUsuario.setTextColor(color)

        } else if (holder is ViewHolderItem) {
            val itemIndex = position - 1
            val jugador = dataSet[itemIndex]

            holder.textViewPosicion.text = position.toString()
            holder.textViewPatosCazados.text = jugador.huntedDucks.toString()
            holder.textViewUsuario.text = jugador.username ?: "Unknown"
        }
    }

    override fun getItemCount(): Int = dataSet.size + 1
}
