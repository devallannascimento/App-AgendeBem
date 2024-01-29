package com.devallannascimento.agendebem

import android.Manifest.permission.POST_NOTIFICATIONS
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import com.devallannascimento.agendebem.databinding.ActivityMainBinding
import com.devallannascimento.agendebem.fragments.AgendamentosFragment
import com.devallannascimento.agendebem.fragments.AgendarFragment
import com.devallannascimento.agendebem.fragments.PerfilFragment
import com.devallannascimento.agendebem.utils.exibirMensagem
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity() {
    
    companion object {
        const val TAG = "info_app"
    }

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val firebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    private val firebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    private val firebaseMessaging by lazy {
        FirebaseMessaging.getInstance()
    }

    val idUsuario = firebaseAuth.currentUser?.uid.toString()

    private var temPermissaoGaleria = false
    private var temPermissaoNotificacoes = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        inicializarToolbar()
        inicializarNavbar()
        solicitarPermissoes()

        messaging()
        loadFragment(AgendarFragment())

    }

    private fun solicitarPermissoes() {

        val listaPermissoesNegadas = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            temPermissaoGaleria = ContextCompat.checkSelfPermission(
                this,
                READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED

            temPermissaoNotificacoes = ContextCompat.checkSelfPermission(
                this,
                POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!temPermissaoNotificacoes) {
                listaPermissoesNegadas.add(POST_NOTIFICATIONS)
            }

            if (!temPermissaoGaleria) {
                listaPermissoesNegadas.add(READ_MEDIA_IMAGES)
            }
            if (listaPermissoesNegadas.isNotEmpty()) {
                val gerenciadorPermissoes = registerForActivityResult(
                    ActivityResultContracts.RequestMultiplePermissions()
                ) { permissoes ->
                    temPermissaoGaleria = permissoes[READ_MEDIA_IMAGES]
                        ?: temPermissaoGaleria

                    temPermissaoNotificacoes = permissoes[POST_NOTIFICATIONS]
                            ?: temPermissaoNotificacoes
                }
                gerenciadorPermissoes.launch(listaPermissoesNegadas.toTypedArray())
            }
        } else {
            temPermissaoGaleria = ContextCompat.checkSelfPermission(
                this,
                READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED

            if (!temPermissaoGaleria) {
                listaPermissoesNegadas.add(READ_EXTERNAL_STORAGE)
            }
            temPermissaoNotificacoes = ContextCompat.checkSelfPermission(
                this,
                "com.google.android.c2dm.permission.RECEIVE"
            ) == PackageManager.PERMISSION_GRANTED

            if (!temPermissaoNotificacoes) {
                listaPermissoesNegadas.add("com.google.android.c2dm.permission.RECEIVE")
            }
            if (listaPermissoesNegadas.isNotEmpty()) {
                val gerenciadorPermissoes = registerForActivityResult(
                    ActivityResultContracts.RequestMultiplePermissions()
                ) { permissoes ->
                    temPermissaoGaleria = permissoes[READ_EXTERNAL_STORAGE]
                        ?: temPermissaoGaleria

                    temPermissaoNotificacoes =
                        permissoes["com.google.android.c2dm.permission.RECEIVE"]
                            ?: temPermissaoNotificacoes
                }
                gerenciadorPermissoes.launch(listaPermissoesNegadas.toTypedArray())
            }
        }
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
            .setNegativeButton("Cancelar"){ _, _ -> }
            .setPositiveButton("Sim"){ _, _ ->
                firebaseAuth.signOut()
                startActivity(
                    Intent(this, LoginActivity::class.java)
                )
            }.create().show()
    }

    private fun messaging(){
        firebaseMessaging
            .token
            .addOnCompleteListener{task ->
                if (task.isSuccessful){
                    val token = task.result
                    firebaseFirestore
                        .collection("usuarios")
                        .document(idUsuario)
                        .update("token",token)
                        .addOnSuccessListener {
                            exibirMensagem("Sucesso ao atualizar FCMToken")
                        }.addOnFailureListener {exception ->
                            exibirMensagem("Erro ao atualizar FCMToken")
                            Log.i(TAG, "messaging: $exception")
                        }
                }
            }
    }

}