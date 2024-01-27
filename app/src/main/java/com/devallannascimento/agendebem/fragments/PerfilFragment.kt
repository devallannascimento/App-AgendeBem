package com.devallannascimento.agendebem.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.devallannascimento.agendebem.R
import com.devallannascimento.agendebem.databinding.FragmentPerfilBinding
import com.devallannascimento.agendebem.utils.exibirMensagem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

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
        recuperarDadosUsuario()

        return this.binding.root
    }

    private fun solicitarPermissoes() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            temPermissaoGaleria = ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED

            if (!temPermissaoGaleria) {
                val gerenciadorPermissoes = registerForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { temPermissao ->
                    temPermissaoGaleria = temPermissao
                }
                gerenciadorPermissoes.launch(Manifest.permission.READ_MEDIA_IMAGES)
            }
        } else {
            temPermissaoGaleria = ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED

            if (!temPermissaoGaleria) {
                val gerenciadorPermissoes = registerForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { temPermissao ->
                    temPermissaoGaleria = temPermissao
                }
                gerenciadorPermissoes.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
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
                        "nome" to binding.editNome.text.toString().trim(),
                        "sobrenome" to binding.editSobrenome.text.toString().trim(),
                        "email" to binding.editEmail.text.toString().trim(),
                        "cpf" to binding.editCpf.text.toString().trim(),
                        "nascimento" to binding.editNascimento.text.toString().trim(),
                        "telefone" to binding.editTelefone.text.toString().trim(),
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
                            atualizarDadosPerfil(idUsuario, dados)
                        }
                    exibirMensagem("Sucesso ao fazer upload")
                }.addOnFailureListener {
                    exibirMensagem("Erro ao fazer upload")
                }
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
        val campos = listOf(
            binding.editNome to "nome",
            binding.editSobrenome to "sobrenome",
            binding.editCpf to "CPF",
            binding.editEmail to "email",
            binding.editNascimento to "nascimento",
            binding.editTelefone to "telefone"
        )

        for ((campo, descricaoCampo) in campos) {
            val valorCampo = campo.text.toString().trim()
            if (valorCampo.isEmpty()) {
                campo.error = "Preencha o seu $descricaoCampo!"
                return false
            } else {
                campo.error = null
            }
        }

        return true
    }

    private fun recuperarDadosUsuario() {
        if (idUsuario != null) {
            lifecycleScope.launch {
                try {
                    val documentSnapshot = firebaseFirestore
                        .collection("usuarios")
                        .document(idUsuario!!)
                        .get()
                        .await()

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

                        Glide.with(this@PerfilFragment)
                            .load(foto)
                            .centerCrop()
                            .placeholder(R.drawable.perfil)
                            .error(R.drawable.perfil)
                            .into(binding.imgPerfil)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}