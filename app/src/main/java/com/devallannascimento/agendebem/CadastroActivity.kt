package com.devallannascimento.agendebem

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.devallannascimento.agendebem.databinding.ActivityCadastroBinding
import com.devallannascimento.agendebem.utils.exibirMensagem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException

class CadastroActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityCadastroBinding.inflate(layoutInflater)
    }

    private lateinit var nome: String
    private lateinit var sobrenome: String
    private lateinit var cpf: String
    private lateinit var email: String
    private lateinit var senha: String

    private val firebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        inicializarToolbar()
        inicializarEventosClique()

    }

    private fun inicializarToolbar() {

        val toolbar = binding.includeToolbar.toolbarPrincipal
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = "Faça o seu cadastro"

            setDisplayHomeAsUpEnabled(true)
        }

    }

    private fun inicializarEventosClique() {

        binding.btnCadastrar.setOnClickListener {
            if (validarCampos()) {
                cadastrarUsuario(nome, senha, email)
            }

        }
    }

    private fun cadastrarUsuario(nome: String, senha: String, email: String) {

        firebaseAuth.createUserWithEmailAndPassword(
            email, senha
        ).addOnCompleteListener { result ->
            if (result.isSuccessful) {
                exibirMensagem("Usuário cadastrado com sucesso")
                startActivity(
                    Intent(applicationContext, MainActivity::class.java)
                )
            }
        }.addOnFailureListener { erro ->
            try {
                throw erro
            } catch (erroSenhaFraca: FirebaseAuthWeakPasswordException) {
                erroSenhaFraca.printStackTrace()
                exibirMensagem("Senha muito fraca, digite outra com mais forte")
            } catch (erroUsuarioExistente: FirebaseAuthUserCollisionException) {
                erroUsuarioExistente.printStackTrace()
                exibirMensagem("E-mail já cadastrado")
            } catch (erroCredenciaisInvalidas: FirebaseAuthInvalidCredentialsException) {
                erroCredenciaisInvalidas.printStackTrace()
                exibirMensagem("E-mail invalido, digite um outro e-mail")
            }
        }

    }

    private fun validarCampos(): Boolean {

        nome = binding.editNome.text.toString()
        sobrenome = binding.editSobrenome.text.toString()
        cpf = binding.editCpf.text.toString()
        email = binding.editEmail.text.toString()
        senha = binding.editSenha.text.toString()

        if (nome.isNotEmpty()) {
            binding.textInputNome.error = null
            if (sobrenome.isNotEmpty()) {
                binding.textInputSobrenome.error = null
                if (cpf.isNotEmpty()) {
                    binding.textInputCpf.error = null
                    if (email.isNotEmpty()) {
                        binding.textInputEmail.error = null
                        if (senha.isNotEmpty()) {
                            binding.textInputSenha.error = null
                            return true
                        } else {
                            binding.textInputSenha.error = "Preencha o seu senha!"
                            return false
                        }
                    } else {
                        binding.textInputEmail.error = "Preencha o seu email!"
                        return false
                    }
                } else {
                    binding.textInputCpf.error = "Preencha o seu CPF!"
                    return false
                }
            } else {
                binding.textInputSobrenome.error = "Preencha o seu sobrenome!"
                return false
            }
        } else {
            binding.textInputNome.error = "Preencha o seu nome!"
            return false
        }
    }

}