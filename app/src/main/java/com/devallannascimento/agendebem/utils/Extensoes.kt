package com.devallannascimento.agendebem.utils

import android.app.Activity
import android.widget.Toast
import android.widget.Toolbar

fun Activity.exibirMensagem(mensagem: String){
    Toast.makeText(this,
        mensagem,
        Toast.LENGTH_LONG
    ).show()
}