package com.devallannascimento.agendebem.model

data class Usuario(
    var id: String,
    var nome: String,
    var sobrenome: String,
    var email: String,
    var cpf: String,
    var nascimento: String,
    var telefone: String,
    var foto: String = ""
)
