package com.devallannascimento.agendebem

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
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
import com.google.firebase.auth.FirebaseAuth

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

    private var temPermissaoGaleria = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        inicializarToolbar()
        inicializarNavbar()
        solicitarPermissoes()
        loadFragment(AgendarFragment())

    }

    private fun solicitarPermissoes() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            temPermissaoGaleria = ContextCompat.checkSelfPermission(
                this,
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
                this,
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

}