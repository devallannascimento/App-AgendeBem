package com.devallannascimento.agendebem.utils

import android.app.Activity
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.devallannascimento.agendebem.R
import com.devallannascimento.agendebem.fragments.AgendarFragment

fun Activity.exibirMensagem(mensagem: String){
    Toast.makeText(this,
        mensagem,
        Toast.LENGTH_LONG
    ).show()
}

fun Fragment.exibirMensagem(mensagem: String){
    Toast.makeText(requireContext(),
        mensagem,
        Toast.LENGTH_LONG
    ).show()
}