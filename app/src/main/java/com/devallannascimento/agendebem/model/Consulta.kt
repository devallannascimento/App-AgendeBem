package com.devallannascimento.agendebem.model

class Consulta(
    val id: String,
    val especialidade: String,
    val medico: String,
    val crm: String,
    val data: String,
    val hora: String,
    val valor: String,
    var disponivel: String
)
