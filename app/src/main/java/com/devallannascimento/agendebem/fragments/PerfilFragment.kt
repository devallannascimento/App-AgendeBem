package com.devallannascimento.agendebem.fragments

import android.Manifest
import android.content.Intent
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
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso

class PerfilFragment : Fragment() {

    private var binding: FragmentPerfilBinding? = null
    private val _binding get() = binding!!

    private val autenticacao by lazy {
        FirebaseAuth.getInstance()
    }
    private val armazenamento by lazy {
        FirebaseStorage.getInstance()
    }

    private var temPermissaoCamera = false
    private var temPermissaoGaleria = false

    private var uriImagemSelecionada: Uri? = null

    private val getContent = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null){
            binding?.imgPerfil?.setImageURI(uri)
            uriImagemSelecionada = uri
            Toast.makeText(requireContext(), "Imagem selecionada", Toast.LENGTH_LONG).show()
            uploadGaleria()
        }else{
            Toast.makeText(requireContext(), "Nenhuma imagem selecionada", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPerfilBinding.inflate(inflater, container, false)

        //solicitarPermissoes()
        inicializarEventosClique()


        return _binding.root
    }

    override fun onStart() {
        super.onStart()
        recuperarImagemFirebase()
        solicitarPermissoes()
    }

    private fun solicitarPermissoes() {

        //Verificar se o usuário já tem permissão\
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

        if (listaPermissoesNegadas.isNotEmpty()){
            //Solicitar multiplas permissões
            val gerenciadorPermissoes = registerForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ){ permissoes ->

                temPermissaoCamera = permissoes[Manifest.permission.CAMERA]
                    ?: temPermissaoCamera

                temPermissaoGaleria = permissoes[Manifest.permission.READ_MEDIA_IMAGES]
                    ?: temPermissaoGaleria

            }
            gerenciadorPermissoes.launch(listaPermissoesNegadas.toTypedArray())
        }
    }

    private fun inicializarEventosClique() {

        binding?.fabSelecionar?.setOnClickListener {
            getContent.launch("image/*")
        }

    }

    private fun uploadGaleria() {
        val idUsuarioLogado = autenticacao.currentUser?.uid
        if (uriImagemSelecionada != null && idUsuarioLogado != null) {
            armazenamento.getReference("fotos")
                .child(idUsuarioLogado)
                .child("foto.jpg")
                .putFile(uriImagemSelecionada!!)
                .addOnSuccessListener { task ->
                    Toast.makeText(requireContext(), "Sucesso ao fazer upload", Toast.LENGTH_LONG).show()
                    task.metadata?.reference?.downloadUrl?.addOnSuccessListener { uriFirebase ->
                        //urlFirebase = uriFirebase.toString()
                        Toast.makeText(requireContext(), uriFirebase.toString(), Toast.LENGTH_LONG).show()
                    }
                }.addOnFailureListener {
                    Toast.makeText(requireContext(), "Erro ao fazer upload", Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun recuperarImagemFirebase(){
        val idUsuarioLogado = autenticacao.currentUser?.uid
        if(idUsuarioLogado != null){
            armazenamento.getReference("fotos")
                .child(idUsuarioLogado)
                .child("foto.jpg")
                .downloadUrl
                .addOnSuccessListener {urlFirebase ->
                    Picasso.get()
                        .load(urlFirebase)
                        .resize(400,400)
                        .into(binding?.imgPerfil)
                }
        }
    }

}