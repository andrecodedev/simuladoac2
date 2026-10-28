package com.mindlab.tarefa2710

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.mindlab.tarefa2710.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val db = FirebaseFirestore.getInstance()
    private val filmesCollection = db.collection("filmes")

    private val listaFilmes = mutableListOf<Filme>()
    private val listaFilmesFiltrada = mutableListOf<Filme>()
    private lateinit var adapter: FilmeAdapter

    private var filmeEmEdicao: Filme? = null

    private val tipos = arrayOf("Filme", "Série", "Documentário", "Animação")
    private val generos = arrayOf("Ação", "Comédia", "Drama", "Terror", "Ficção Científica", "Romance", "Aventura", "Outro")
    private val filtrosTipo = arrayOf("Todos", "Filme", "Série", "Documentário", "Animação")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configurarSpinners()
        configurarRecyclerView()
        configurarBotoes()
        configurarBuscaEFiltro()
        carregarFilmesDoFirestore()
    }

    private fun configurarSpinners() {
        val adapterTipo = ArrayAdapter(this, android.R.layout.simple_spinner_item, tipos)
        adapterTipo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerTipo.adapter = adapterTipo

        val adapterGenero = ArrayAdapter(this, android.R.layout.simple_spinner_item, generos)
        adapterGenero.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerGenero.adapter = adapterGenero

        val adapterFiltro = ArrayAdapter(this, android.R.layout.simple_spinner_item, filtrosTipo)
        adapterFiltro.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerFiltroTipo.adapter = adapterFiltro

        binding.spinnerFiltroTipo.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                aplicarFiltrosEBusca()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun configurarRecyclerView() {
        adapter = FilmeAdapter(
            listaFilmesFiltrada,
            onClick = { filme -> prepararEdicao(filme) },
            onLongClick = { filme -> confirmarExclusao(filme) }
        )
        binding.recyclerViewFilmes.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewFilmes.adapter = adapter
    }

    private fun configurarBotoes() {
        binding.btnSalvar.setOnClickListener {
            salvarOuAtualizarFilme()
        }

        binding.btnCancelar.setOnClickListener {
            limparFormulario()
        }
    }

    private fun configurarBuscaEFiltro() {
        binding.etBusca.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                aplicarFiltrosEBusca()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun carregarFilmesDoFirestore() {
        filmesCollection.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Toast.makeText(this, "Erro ao carregar dados: ${e.message}", Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }

            if (snapshot != null) {
                listaFilmes.clear()
                for (document in snapshot.documents) {
                    val filme = document.toObject(Filme::class.java)
                    if (filme != null) {
                        filme.id = document.id
                        listaFilmes.add(filme)
                    }
                }
                aplicarFiltrosEBusca()
            }
        }
    }

    private fun salvarOuAtualizarFilme() {
        val nome = binding.etNome.text.toString().trim()
        val tipo = binding.spinnerTipo.selectedItem.toString()
        val genero = binding.spinnerGenero.selectedItem.toString()
        val anoStr = binding.etAno.text.toString().trim()
        val notaStr = binding.etNota.text.toString().trim()
        val assistido = binding.cbAssistido.isChecked

        // Validações mínimas
        if (nome.isEmpty()) {
            binding.etNome.error = "O nome não pode estar vazio"
            binding.etNome.requestFocus()
            return
        }
        if (anoStr.isEmpty()) {
            binding.etAno.error = "Informe o ano de lançamento"
            binding.etAno.requestFocus()
            return
        }
        if (notaStr.isEmpty()) {
            binding.etNota.error = "Informe a nota pessoal"
            binding.etNota.requestFocus()
            return
        }

        val ano = anoStr.toIntOrNull()
        if (ano == null || ano <= 1800 || ano > 2100) {
            binding.etAno.error = "Ano inválido"
            binding.etAno.requestFocus()
            return
        }

        val nota = notaStr.toDoubleOrNull()
        if (nota == null || nota < 0.0 || nota > 10.0) {
            binding.etNota.error = "A nota deve ser entre 0.0 e 10.0"
            binding.etNota.requestFocus()
            return
        }

        val filmeId = filmeEmEdicao?.id ?: filmesCollection.document().id
        val filme = Filme(
            id = filmeId,
            nome = nome,
            tipo = tipo,
            genero = genero,
            ano = ano,
            nota = nota,
            assistido = assistido
        )

        filmesCollection.document(filmeId).set(filme)
            .addOnSuccessListener {
                val mensagem = if (filmeEmEdicao == null) "Cadastrado com sucesso!" else "Atualizado com sucesso!"
                Toast.makeText(this, mensagem, Toast.LENGTH_SHORT).show()
                limparFormulario()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao salvar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun prepararEdicao(filme: Filme) {
        filmeEmEdicao = filme
        binding.etNome.setText(filme.nome)
        binding.etAno.setText(filme.ano.toString())
        binding.etNota.setText(filme.nota.toString())
        binding.cbAssistido.isChecked = filme.assistido

        // Selecionar spinner tipo
        val posTipo = tipos.indexOf(filme.tipo)
        if (posTipo >= 0) binding.spinnerTipo.setSelection(posTipo)

        // Selecionar spinner genero
        val posGenero = generos.indexOf(filme.genero)
        if (posGenero >= 0) binding.spinnerGenero.setSelection(posGenero)

        binding.btnSalvar.text = "Atualizar"
        binding.btnCancelar.visibility = View.VISIBLE
    }

    private fun confirmarExclusao(filme: Filme) {
        AlertDialog.Builder(this)
            .setTitle("Excluir Registro")
            .setMessage("Deseja realmente excluir '${filme.nome}'?")
            .setPositiveButton("Sim") { _, _ ->
                excluirFilme(filme.id)
            }
            .setNegativeButton("Não", null)
            .show()
    }

    private fun excluirFilme(id: String) {
        filmesCollection.document(id).delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Excluído com sucesso!", Toast.LENGTH_SHORT).show()
                if (filmeEmEdicao?.id == id) {
                    limparFormulario()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao excluir: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun limparFormulario() {
        filmeEmEdicao = null
        binding.etNome.text?.clear()
        binding.etAno.text?.clear()
        binding.etNota.text?.clear()
        binding.cbAssistido.isChecked = false
        binding.spinnerTipo.setSelection(0)
        binding.spinnerGenero.setSelection(0)
        binding.btnSalvar.text = "Salvar"
        binding.btnCancelar.visibility = View.GONE
        binding.etNome.error = null
        binding.etAno.error = null
        binding.etNota.error = null
    }

    private fun aplicarFiltrosEBusca() {
        val queryBusca = binding.etBusca.text.toString().trim().lowercase()
        val filtroTipoSelecionado = binding.spinnerFiltroTipo.selectedItem.toString()

        listaFilmesFiltrada.clear()
        for (filme in listaFilmes) {
            val matchBusca = filme.nome.lowercase().contains(queryBusca)
            val matchTipo = filtroTipoSelecionado == "Todos" || filme.tipo == filtroTipoSelecionado

            if (matchBusca && matchTipo) {
                listaFilmesFiltrada.add(filme)
            }
        }
        adapter.atualizarLista(listaFilmesFiltrada)
    }
}
