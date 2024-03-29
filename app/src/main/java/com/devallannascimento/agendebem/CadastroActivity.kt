package com.devallannascimento.agendebem

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.devallannascimento.agendebem.databinding.ActivityCadastroBinding
import com.devallannascimento.agendebem.model.Usuario
import com.devallannascimento.agendebem.utils.exibirMensagem
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthEmailException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore

class CadastroActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityCadastroBinding.inflate(layoutInflater)
    }

    private lateinit var nomeUsuario: String
    private lateinit var sobrenomeUsuario: String
    private lateinit var cpfUsuario: String
    private lateinit var emailUsuario: String
    private lateinit var nascimentoUsuario: String
    private lateinit var telefoneUsuario: String
    private lateinit var senhaUsuario: String

    private val firebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    private val firebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        inicializarToolbar()
        inicializarEventosClique()

    }

    private fun inicializarToolbar() {
        val toolbar = binding.includeToolbar.tbPrincipal
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = "Faça o seu cadastro"
            setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun inicializarEventosClique() {

        binding.btnCadastrar.setOnClickListener {
            if (validarCampos()) {
                cadastrarUsuario(
                    nomeUsuario,
                    sobrenomeUsuario,
                    emailUsuario,
                    cpfUsuario,
                    nascimentoUsuario,
                    telefoneUsuario,
                    senhaUsuario
                )
            }
        }

        binding.textFacaLogin.setOnClickListener {
            startActivity(
                Intent(this, LoginActivity::class.java)
            )
        }

    }

    private fun cadastrarUsuario(
        nome: String,
        sobrenome: String,
        email: String,
        cpf: String,
        nascimento: String,
        telefone: String,
        senha: String
    ) {
        firebaseAuth.createUserWithEmailAndPassword(
            email, senha
        ).addOnSuccessListener { _ ->
            val idUsuario = firebaseAuth.currentUser?.uid
            if (idUsuario != null) {
                val usuario = Usuario(
                    idUsuario, nome, sobrenome, email, cpf, nascimento, telefone
                )
                salvarUsuarioFirestore(usuario)
            }
        }.addOnFailureListener { erro ->
            when (erro) {
                is FirebaseAuthWeakPasswordException -> {
                    erro.printStackTrace()
                    exibirMensagem("Senha muito fraca, digite outra com mais forte")
                }

                is FirebaseAuthUserCollisionException -> {
                    erro.printStackTrace()
                    exibirMensagem("E-mail já cadastrado")
                }

                is FirebaseAuthInvalidCredentialsException -> {
                    erro.printStackTrace()
                    exibirMensagem("E-mail invalido, digite um outro e-mail")
                }

                is FirebaseAuthEmailException -> {
                    erro.printStackTrace()
                    exibirMensagem("Erro ao tentar usar este e-mail, tente com um outro e-mail")
                }

                is FirebaseNetworkException -> {
                    erro.printStackTrace()
                    exibirMensagem("Erro! Tente novamente")
                }

                else -> {
                    exibirMensagem("Erro desconhecido")
                }
            }
        }

    }

    private fun salvarUsuarioFirestore(usuario: Usuario) {
        firebaseFirestore
            .collection("usuarios")
            .document(usuario.id)
            .set(usuario)
            .addOnSuccessListener {

                exibirMensagem("Usuário cadastrado com sucesso")
                startActivity(
                    Intent(applicationContext, MainActivity::class.java)
                )
            }
            .addOnFailureListener {
                exibirMensagem("Erro ao cadastrar usuário")
            }
    }

    private fun validarCampos(): Boolean {

        nomeUsuario = binding.editNome.text.toString().trim()
        sobrenomeUsuario = binding.editSobrenome.text.toString().trim()
        emailUsuario = binding.editEmail.text.toString().trim()
        cpfUsuario = binding.editCpf.text.toString().trim()
        nascimentoUsuario = binding.editNascimento.text.toString().trim()
        telefoneUsuario = binding.editTelefone.text.toString().trim()
        senhaUsuario = binding.editSenha.text.toString().trim()

        if (nomeUsuario.isNotEmpty()) {
            binding.textInputNome.error = null
            if (sobrenomeUsuario.isNotEmpty()) {
                binding.textInputSobrenome.error = null
                if (cpfUsuario.isNotEmpty()) {
                    binding.textInputCpf.error = null
                    if (emailUsuario.isNotEmpty()) {
                        binding.textInputEmail.error = null
                        if (nascimentoUsuario.isNotEmpty()) {
                            binding.textInputNascimento.error = null
                            return if (telefoneUsuario.isNotEmpty()) {
                                binding.textInputTelefone.error = null
                                if (senhaUsuario.isNotEmpty()) {
                                    binding.textInputSenha.error = null
                                    true
                                } else {
                                    binding.textInputSenha.error = "Preencha a sua senha!"
                                    false
                                }
                            } else {
                                binding.textInputTelefone.error = "Preencha o seu telefone!"
                                false
                            }
                        } else {
                            binding.textInputNascimento.error = "Preencha o seu nascimento!"
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