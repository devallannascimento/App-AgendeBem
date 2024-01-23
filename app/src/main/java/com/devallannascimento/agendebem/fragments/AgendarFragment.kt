package com.devallannascimento.agendebem.fragments

import ConsultasAdapter
import android.R
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devallannascimento.agendebem.MainActivity.Companion.TAG
import com.devallannascimento.agendebem.databinding.FragmentAgendarBinding
import com.devallannascimento.agendebem.model.Consulta
import com.devallannascimento.agendebem.utils.exibirMensagem
import com.devallannascimento.agendebem.utils.restartFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AgendarFragment : Fragment() {

    private var agendarBinding: FragmentAgendarBinding? = null
    private val binding get() = this.agendarBinding!!

    private val firebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    private val firebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    private val idUsuario = firebaseAuth.currentUser?.uid

    // Lista local de consultas
    private val especialidadesList = mutableListOf<String>()

    // Declare consultasAdapter como uma propriedade da classe
    private val consultasAdapter = ConsultasAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        this.agendarBinding = FragmentAgendarBinding.inflate(inflater, container, false)
        inicializarSpinner()

        return this.binding.root
    }

    private fun inicializarSpinner() {
        firebaseFirestore.collection("consultas").get()
            .addOnSuccessListener { querySnapshot ->
                querySnapshot.forEach { document ->
                    val nomeEspecialidade = document.getString("especialidade")
                    if (nomeEspecialidade != null && !especialidadesList.contains(nomeEspecialidade)) {
                        especialidadesList.add(nomeEspecialidade)
                    }
                }
                val adapter =
                    ArrayAdapter(requireContext(), R.layout.simple_spinner_item, especialidadesList)
                adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
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
                            recuperarConsultas(especialidadeSelecionada)
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

    private fun recuperarConsultas(especialidade: String) {

        val consultasCollection = FirebaseFirestore.getInstance().collection("consultas")

        // Filtrar as consultas pela especialidade
        val consultaQuery = consultasCollection.whereEqualTo("especialidade", especialidade)

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

                    val consulta =
                        Consulta(id, especialidade, medico, crm, data, hora, valor, disponivel)
                    if (disponivel == "true") {
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

        // Configurar o ItemClickListener para o RecyclerView
        recyclerView.addOnItemTouchListener(
            object : RecyclerView.OnItemTouchListener {
                override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {
                    // Não é necessário implementar neste exemplo
                }

                override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                    val child = rv.findChildViewUnder(e.x, e.y)
                    if (child != null && e.action == MotionEvent.ACTION_UP) {
                        val position = rv.getChildAdapterPosition(child)
                        // Lidar com o clique no item
                        exibirDialogoConfirmacao(position)
                    }
                    return false
                }

                override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
                    // Não é necessário implementar neste exemplo
                }
            }
        )

    }

    // Dentro da função exibirDialogoConfirmacao
    fun exibirDialogoConfirmacao(position: Int) {

        val consultaSelecionada = consultasAdapter.getConsulta(position)
        val id = consultaSelecionada.id
        val especialidade = consultaSelecionada.especialidade
        val medico = consultaSelecionada.medico
        val crm = consultaSelecionada.crm
        val data = consultaSelecionada.data
        val hora = consultaSelecionada.hora
        val valor = consultaSelecionada.valor

        val consulta = Consulta(
            id,
            especialidade,
            medico,
            crm,
            data,
            hora,
            valor,
            "true"
        )

        AlertDialog.Builder(requireContext())
            .setTitle("Vamos confirmar a consulta?")
            .setMessage("\nConfirmar o agendamento da consulta de $especialidade para o dia:\n\n$data às $hora?")
            .setPositiveButton("Sim") { _, _ ->
                // Atualizar o campo "disponivel" no Firebase Firestore
                atualizarDisponibilidadeConsulta(consultaSelecionada)
                // Criar um novo documento na coleção "usuario"
                criarDocumentoUsuario(consulta)
            }
            .setNegativeButton("Não") { _, _ ->
                // Lógica para lidar com o não
            }
            .create().show()
    }

    // Função para atualizar o campo "disponivel" no Firebase Firestore
    private fun atualizarDisponibilidadeConsulta(consulta: Consulta) {
        val consultasCollection = FirebaseFirestore.getInstance().collection("consultas")

        // Atualizar o campo "disponivel" para "false"
        consultasCollection.document(consulta.id)
            .update("disponivel", "false")
            .addOnSuccessListener {
                Log.d(TAG, "Campo 'disponivel' atualizado com sucesso.")
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Erro ao atualizar o campo 'disponivel': $exception")
            }
    }

    // Função para criar um documento na coleção "usuario"
    private fun criarDocumentoUsuario(consulta: Consulta) {

        firebaseFirestore
            .collection("usuarios")
            .document(idUsuario!!)
            .collection("consultas")
            .document(consulta.id)
            .set(consulta)
            .addOnSuccessListener {
                exibirMensagem("Sucesso ao marcar consulta")
                restartFragment()
            }
            .addOnFailureListener {
                exibirMensagem("Erro ao marcar consulta")
            }
    }

}