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

// Função para reiniciar o fragmento
fun Fragment.restartFragment() {
    val fragmentTransaction = requireFragmentManager().beginTransaction()
    val currentFragment = requireFragmentManager().findFragmentById(R.id.fragmentConteudo)

    if (currentFragment != null) {
        fragmentTransaction.remove(currentFragment)
    }

    val newFragment = AgendarFragment()
    fragmentTransaction.replace(R.id.fragmentConteudo, newFragment)
    fragmentTransaction.addToBackStack(null)
    fragmentTransaction.commit()
}