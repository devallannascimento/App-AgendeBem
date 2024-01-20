package com.devallannascimento.agendebem.model

data class Usuario(
    var id: String,
    var nome: String,
    var sobrenome: String,
    var cpf: String,
    var email: String,
    var foto: String = ""
)
