package com.devallannascimento.agendebem.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.devallannascimento.agendebem.utils.exibirMensagem
import com.devallannascimento.agendebem.databinding.FragmentPerfilBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso

class PerfilFragment : Fragment() {

    private var perfilBinding: FragmentPerfilBinding? = null
    private val binding get() = this.perfilBinding!!

    private val firebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }
    private val firebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }
    private val firebaseStorage by lazy {
        FirebaseStorage.getInstance()
    }

    private var idUsuario: String? = null

    private lateinit var nomeUsuario: String
    private lateinit var sobrenomeUsuario: String
    private lateinit var emailUsuario: String
    private lateinit var cpfUsuario: String
    private lateinit var nascimentoUsuario: String
    private lateinit var telefoneUsuario: String
    private lateinit var fotoUsuario: String

    private var temPermissaoCamera = false
    private var temPermissaoGaleria = false

    private var uriImagemSelecionada: Uri? = null

    private val getContent = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            this.binding.imgPerfil.setImageURI(uri)
            uriImagemSelecionada = uri
            Toast.makeText(requireContext(), "Imagem selecionada", Toast.LENGTH_LONG).show()
            uploadGaleria(uri)
        } else {
            Toast.makeText(requireContext(), "Nenhuma imagem selecionada", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        this.perfilBinding = FragmentPerfilBinding.inflate(inflater, container, false)

        idUsuario = firebaseAuth.currentUser?.uid

        solicitarPermissoes()
        inicializarEventosClique()

        return this.binding.root
    }

    override fun onStart() {
        super.onStart()
        recuperarDadosUsuario()
    }

    private fun solicitarPermissoes() {

        //Verificar se o usuário já tem permissão
        temPermissaoCamera = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        temPermissaoGaleria = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.READ_MEDIA_IMAGES
        ) == PackageManager.PERMISSION_GRANTED

        //Lista permissoões negadas
        val listaPermissoesNegadas = mutableListOf<String>()
        if (!temPermissaoCamera) {
            listaPermissoesNegadas.add(Manifest.permission.CAMERA)
        }
        if (!temPermissaoGaleria) {
            listaPermissoesNegadas.add(Manifest.permission.READ_MEDIA_IMAGES)
        }

        if (listaPermissoesNegadas.isNotEmpty()) {
            //Solicitar multiplas permissões
            val gerenciadorPermissoes = registerForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { permissoes ->

                temPermissaoCamera = permissoes[Manifest.permission.CAMERA]
                    ?: temPermissaoCamera

                temPermissaoGaleria = permissoes[Manifest.permission.READ_MEDIA_IMAGES]
                    ?: temPermissaoGaleria

            }
            gerenciadorPermissoes.launch(listaPermissoesNegadas.toTypedArray())
        }
    }

    private fun inicializarEventosClique() {

        this.binding.fabSelecionar.setOnClickListener {
            if (temPermissaoGaleria) {
                getContent.launch("image/*")
            } else {
                exibirMensagem("Não tem permisssão para acessar a galeria")
            }
        }

        this.binding.btnSalvar.setOnClickListener {

            val idUsuario = firebaseAuth.currentUser?.uid
            if (validarCampos()) {
                if (idUsuario != null) {

                    val dados = mapOf(
                        "nome" to nomeUsuario,
                        "sobrenome" to sobrenomeUsuario,
                        "email" to emailUsuario,
                        "cpf" to cpfUsuario,
                        "nascimento" to nascimentoUsuario,
                        "telefone" to telefoneUsuario
                    )
                    atualizarDadosPerfil(idUsuario, dados)

                } else {
                    exibirMensagem("Preencha todos os campos para atualizar")
                }
            }
        }

    }

    private fun uploadGaleria(uri: Uri) {
        val idUsuario = firebaseAuth.currentUser?.uid
        if (idUsuario != null) {
            firebaseStorage
                .getReference("fotos")
                .child("usuarios")
                .child(idUsuario)
                .child("perfil.jpg")
                .putFile(uri)
                .addOnSuccessListener { task ->
                    task.metadata
                        ?.reference
                        ?.downloadUrl
                        ?.addOnSuccessListener { url ->
                            val dados = mapOf(
                                "foto" to url.toString()
                            )
                            atualizarFotoPerfil(idUsuario, dados)
                        }
                    exibirMensagem("Sucesso ao fazer upload")
                }.addOnFailureListener {
                    exibirMensagem("Erro ao fazer upload")
                }
        }
    }

    private fun atualizarFotoPerfil(idUsuario: String, dados: Map<String, String>) {
        firebaseFirestore.collection("usuarios")
            .document(idUsuario)
            .update(dados)
            .addOnSuccessListener {
                exibirMensagem("Sucesso ao atualizar perfil do usuário")
            }.addOnFailureListener {
                exibirMensagem("Erro ao atualizar perfil do usuário")
            }
    }

    private fun atualizarDadosPerfil(idUsuario: String, dados: Map<String, String>) {
        firebaseFirestore.collection("usuarios")
            .document(idUsuario)
            .update(dados)
            .addOnSuccessListener {
                exibirMensagem("Sucesso ao atualizar perfil do usuário")
            }.addOnFailureListener {
                exibirMensagem("Erro ao atualizar perfil do usuário")
            }
    }

    private fun validarCampos(): Boolean {

        nomeUsuario = binding.editNome.text.toString()
        sobrenomeUsuario = binding.editSobrenome.text.toString()
        emailUsuario = binding.editEmail.text.toString()
        cpfUsuario = binding.editCpf.text.toString()
        nascimentoUsuario = binding.editNascimento.text.toString()
        telefoneUsuario = binding.editTelefone.text.toString()

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
                            if (telefoneUsuario.isNotEmpty()) {
                                binding.textInputTelefone.error = null
                                return true
                            } else {
                                binding.textInputTelefone.error = "Preencha o seu telefone!"
                                return false
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

    private fun recuperarDadosUsuario() {

        if (idUsuario != null) {
            firebaseFirestore
                .collection("usuarios")
                .document(idUsuario!!)
                .get()
                .addOnSuccessListener {documentSnapshot ->
                    val dadosUsuario = documentSnapshot.data
                    if (dadosUsuario != null) {
                        val nome = dadosUsuario["nome"] as String
                        val sobrenome = dadosUsuario["sobrenome"] as String
                        val email = dadosUsuario["email"] as String
                        val cpf = dadosUsuario["cpf"] as String
                        val nascimento = dadosUsuario["nascimento"] as String
                        val telefone = dadosUsuario["telefone"] as String
                        val foto = dadosUsuario["foto"] as String

                        binding.editNome.setText(nome)
                        binding.editSobrenome.setText(sobrenome)
                        binding.editEmail.setText(email)
                        binding.editCpf.setText(cpf)
                        binding.editNascimento.setText(nascimento)
                        binding.editTelefone.setText(telefone)
                        if (foto.isNotEmpty()){
                            Picasso.get()
                                .load(foto)
                                .into(binding.imgPerfil)
                        }

                    }
                }
        }
    }
}