package com.devallannascimento.agendebem

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import com.devallannascimento.agendebem.databinding.ActivityMainBinding
import com.devallannascimento.agendebem.fragments.AgendamentosFragment
import com.devallannascimento.agendebem.fragments.AgendarFragment
import com.devallannascimento.agendebem.fragments.PerfilFragment
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val firebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    private var temPermissaoCamera = false
    private var temPermissaoGaleria = false
    private var temPermissaoLocalizacao = false
    private var temPermissaoNotificacoes = false

    private var uriImagemSelecionada: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        Log.i("info_app", "onCreate: sucesso ")

        inicializarToolbar()
        inicializarNavbar()
        solicitarPermissoes()
        loadFragment(AgendarFragment())

    }

    private fun solicitarPermissoes() {

        //Verificar se o usuário já tem permissão\
        temPermissaoCamera = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        temPermissaoGaleria = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_MEDIA_IMAGES
        ) == PackageManager.PERMISSION_GRANTED

        temPermissaoLocalizacao = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        temPermissaoNotificacoes = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

        //Lista permissoões negadas
        val listaPermissoesNegadas = mutableListOf<String>()
        if (!temPermissaoCamera) {
            listaPermissoesNegadas.add(Manifest.permission.CAMERA)
        }
        if (!temPermissaoGaleria) {
            listaPermissoesNegadas.add(Manifest.permission.READ_MEDIA_IMAGES)
        }
        if (!temPermissaoLocalizacao) {
            listaPermissoesNegadas.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        if (!temPermissaoNotificacoes) {
            listaPermissoesNegadas.add(Manifest.permission.POST_NOTIFICATIONS)
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

                temPermissaoLocalizacao = permissoes[Manifest.permission.ACCESS_COARSE_LOCATION]
                    ?: temPermissaoLocalizacao

                temPermissaoNotificacoes = permissoes[Manifest.permission.POST_NOTIFICATIONS]
                    ?: temPermissaoNotificacoes
            }
            gerenciadorPermissoes.launch(listaPermissoesNegadas.toTypedArray())
        }
        }

    override fun onStart() {
        super.onStart()
    }

    private fun inicializarToolbar() {
        val toolbar = binding.includeMainToolbar.tbPrincipal
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = "Agendar consulta"
        }

        addMenuProvider(
            object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.menu_perfil, menu)
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    when(menuItem.itemId) {
                        R.id.item_sair-> {
                            deslogarUsuario()
                        }
                    }
                    return true
                }
            }
        )


    }

    private fun inicializarNavbar() {
        val navbar = binding.navbarPrincipal
        navbar.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_agendar -> {
                    loadFragment(AgendarFragment())
                    supportActionBar?.title = "Agendar consulta"
                    true
                }
                R.id.menu_agendamentos -> {
                    loadFragment(AgendamentosFragment())
                    supportActionBar?.title = "Meus agendamentos"
                    true
                }
                R.id.menu_perfil -> {
                    loadFragment(PerfilFragment())
                    supportActionBar?.title = "Meu perfil"
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentConteudo, fragment)
            .commit()
    }

    private fun deslogarUsuario(){

        AlertDialog.Builder(this)
            .setTitle("Deslogar")
            .setMessage("Deseja realmente sair?")
            .setNegativeButton("Cancelar"){dialog, posicao -> }
            .setPositiveButton("Sim"){dialog, posicao ->
                firebaseAuth.signOut()
                startActivity(
                    Intent(this, LoginActivity::class.java)
                )
            }.create().show()
    }

}