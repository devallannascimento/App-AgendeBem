package com.devallannascimento.agendebem

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.devallannascimento.agendebem.databinding.ActivityLoginBinding
import com.devallannascimento.agendebem.utils.exibirMensagem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthEmailException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.auth.FirebaseAuthCredentialsProvider

class LoginActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }

    private lateinit var email: String
    private lateinit var senha: String

    private val firebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        inicializarCliques()

    }

    override fun onStart() {
        super.onStart()
        verificarUsuarioLogado()
    }

    private fun verificarUsuarioLogado() {
        val usuarioAtual = firebaseAuth.currentUser

        if (usuarioAtual != null) {
            startActivity(
                Intent(this, MainActivity::class.java)
            )
        }
    }

    private fun inicializarCliques() {

        binding.btnLogin.setOnClickListener {
            if (validarCampos()) {
                logarUsuario()
            }
        }

        binding.textRecuperar.setOnClickListener {
            val email = binding.editEmail.text.toString().trim()
            if (email.isNotEmpty()) {
                binding.textInputEmail.error = null
                enviarEmailRecuperacao(email)
            } else {
                binding.textInputEmail.error = "Preencha o seu email!"
                exibirMensagem("Digite um endereço de e-mail válido.")
            }
        }

        binding.textRegistro.setOnClickListener {
            startActivity(
                Intent(this, CadastroActivity::class.java)
            )
        }

    }

    private fun enviarEmailRecuperacao(email: String) {
        firebaseAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    exibirMensagem("Email de recuperação enviado. Verifique sua caixa de entrada.")
                } else {
                    exibirMensagem("Falha ao enviar email de recuperação. Verifique o endereço de e-mail.")
                }
            }
    }

    private fun logarUsuario() {

        firebaseAuth.signInWithEmailAndPassword(
            email, senha
        ).addOnSuccessListener {
            exibirMensagem("Usuário logado com sucesso")
            startActivity(
                Intent(this, MainActivity::class.java)
            )
        }.addOnFailureListener { error ->
            when (error) {
                is FirebaseAuthInvalidUserException -> {
                    error.printStackTrace()
                    exibirMensagem("E-mail não cadastrado")
                }

                is FirebaseAuthInvalidUserException -> {
                    error.printStackTrace()
                    exibirMensagem("E-mail não cadastrado")
                }

                is FirebaseAuthInvalidCredentialsException -> {
                    error.printStackTrace()
                    exibirMensagem("E-mail ou senha estão incorretos!")
                }

                else -> {
                    exibirMensagem("Erro desconhecido")
                }
            }
        }
    }

    private fun validarCampos(): Boolean {

        email = binding.editEmail.text.toString()
        senha = binding.editSenha.text.toString()

        if (email.isNotEmpty()) {
            binding.textInputEmail.error = null
            if (senha.isNotEmpty()) {
                binding.textInputSenha.error = null
                return true
            } else {
                binding.textInputSenha.error = "Preencha com a sua!"
                return false
            }
        } else {
            binding.textInputEmail.error = "Preencha com o seu email!"
            return false
        }

    }
}