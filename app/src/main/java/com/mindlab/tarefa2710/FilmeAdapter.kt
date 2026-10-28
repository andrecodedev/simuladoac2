package com.mindlab.tarefa2710

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FilmeAdapter(
    private var filmes: List<Filme>,
    private val onClick: (Filme) -> Unit,
    private val onLongClick: (Filme) -> Unit
) : RecyclerView.Adapter<FilmeAdapter.FilmeViewHolder>() {

    class FilmeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNome: TextView = itemView.findViewById(R.id.tvNome)
        val tvNota: TextView = itemView.findViewById(R.id.tvNota)
        val tvDetalhes: TextView = itemView.findViewById(R.id.tvDetalhes)
        val tvAssistido: TextView = itemView.findViewById(R.id.tvAssistido)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilmeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_filme, parent, false)
        return FilmeViewHolder(view)
    }

    override fun onBindViewHolder(holder: FilmeViewHolder, position: Int) {
        val filme = filmes[position]
        holder.tvNome.text = filme.nome
        holder.tvNota.text = "⭐ ${filme.nota}"
        holder.tvDetalhes.text = "${filme.tipo} • ${filme.genero} • ${filme.ano}"
        
        if (filme.assistido) {
            holder.tvAssistido.text = "✓ Assistido"
            holder.tvAssistido.visibility = View.VISIBLE
        } else {
            holder.tvAssistido.visibility = View.GONE
        }

        holder.itemView.setOnClickListener { onClick(filme) }
        holder.itemView.setOnLongClickListener {
            onLongClick(filme)
            true
        }
    }

    override fun getItemCount(): Int = filmes.size

    fun atualizarLista(novaLista: List<Filme>) {
        filmes = novaLista
        notifyDataSetChanged()
    }
}
