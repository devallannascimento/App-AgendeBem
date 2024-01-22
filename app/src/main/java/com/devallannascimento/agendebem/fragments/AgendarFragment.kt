package com.devallannascimento.agendebem.fragments

import ConsultasAdapter
import android.R
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devallannascimento.agendebem.MainActivity.Companion.TAG
import com.devallannascimento.agendebem.databinding.FragmentAgendarBinding
import com.devallannascimento.agendebem.model.Consulta
import com.google.firebase.firestore.FirebaseFirestore

class AgendarFragment : Fragment() {

    private var agendarBinding: FragmentAgendarBinding? = null
    private val binding get() = this.agendarBinding!!

    private val firebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    // Lista local de consultas
    private val consultasList = mutableListOf<Consulta>()

    // Declare consultasAdapter como uma propriedade da classe
    private val consultasAdapter = ConsultasAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        this.agendarBinding = FragmentAgendarBinding.inflate(inflater, container, false)

        inicializarSpinner()

        return this.binding.root
    }

    private fun inicializarSpinner() {
        firebaseFirestore.collection("consultas").get()
            .addOnSuccessListener { querySnapshot ->
                val especialidadesList = mutableListOf<String>()
                querySnapshot.forEach { document ->
                    val nomeEspecialidade = document.getString("especialidade")
                    if (nomeEspecialidade != null) {
                        especialidadesList.add(nomeEspecialidade)
                    }
                }
                val adapter =
                    ArrayAdapter(requireContext(), R.layout.simple_spinner_item, especialidadesList)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spinnerEspecialidades.adapter = adapter

                // Adicionar um ouvinte para responder à seleção no Spinner
                binding.spinnerEspecialidades.onItemSelectedListener =
                    object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            // Recuperar a especialidade selecionada
                            val especialidadeSelecionada = especialidadesList[position]

                            val consultasCollection = FirebaseFirestore.getInstance().collection("consultas")

                            // Filtrar as consultas pela especialidade
                            val consultaQuery = consultasCollection.whereEqualTo("especialidade", especialidadeSelecionada)

                            // Recuperar os dados e exibir no RecyclerView
                            consultaQuery.get()
                                .addOnSuccessListener { querySnapshot ->
                                    val consultasList = mutableListOf<Consulta>()

                                    querySnapshot.documents.forEach { document ->

                                        // Converter documento para objeto Consulta
                                        val id = document.id
                                        val especialidade = document.getString("especialidade") ?: ""
                                        val medico = document.getString("medico") ?: ""
                                        val crm = document.getString("crm") ?: ""
                                        val data = document.getString("data") ?: ""
                                        val hora = document.getString("hora") ?: ""
                                        val valor = document.getString("valor") ?: ""
                                        val disponivel = document.getString("disponivel") ?: ""

                                        val consulta = Consulta(id, especialidade, medico, crm, data, hora, valor, disponivel)
                                        if (disponivel == "true"){
                                            consultasList.add(consulta)
                                        }
                                    }
                                    // Agora, após recuperar os dados, inicialize o RecyclerView
                                    inicializarRecyclerView(consultasList)
                                }
                                .addOnFailureListener { exception ->
                                    // Lidar com falhas na recuperação dos documentos
                                    Log.e(TAG, "Erro ao obter documentos: $exception")
                                }
                        }

                        override fun onNothingSelected(parent: AdapterView<*>?) {
                            // Ação quando nada é selecionado no Spinner
                        }
                    }

            }.addOnFailureListener { exception ->
                // Trate falhas na obtenção dos documentos
                Log.e(TAG, "Erro ao obter documentos: $exception")
            }
    }

    private fun inicializarRecyclerView(consultasList: List<Consulta>) {
        // Configurar o RecyclerView
        val recyclerView: RecyclerView = binding.recyclerViewConsultas

        // Crie e configure um LinearLayoutManager (ou GridLayoutManager, se preferir)
        val layoutManager = LinearLayoutManager(requireContext())
        recyclerView.layoutManager = layoutManager

        // Use o adaptador já declarado como uma propriedade da classe
        recyclerView.adapter = consultasAdapter

        // Agora atualize o adaptador com as consultas
        consultasAdapter.atualizarConsultas(consultasList)
    }


}