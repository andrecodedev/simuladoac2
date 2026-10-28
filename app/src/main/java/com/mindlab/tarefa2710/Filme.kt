package com.mindlab.tarefa2710

data class Filme(
    var id: String = "",
    val nome: String = "",
    val tipo: String = "",
    val genero: String = "",
    val ano: Int = 0,
    val nota: Double = 0.0,
    val assistido: Boolean = false
)
