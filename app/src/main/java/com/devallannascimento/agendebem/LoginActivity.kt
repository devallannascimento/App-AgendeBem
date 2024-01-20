package com.devallannascimento.agendebem

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.devallannascimento.agendebem.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        inicializarCliques()

    }

    private fun inicializarCliques() {

        binding.textRegistro.setOnClickListener {
            startActivity(
                Intent(this, CadastroActivity::class.java)
            )
        }

    }
}