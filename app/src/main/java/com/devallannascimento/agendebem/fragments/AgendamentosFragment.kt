package com.devallannascimento.agendebem.fragments

import ConsultasAdapter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devallannascimento.agendebem.MainActivity
import com.devallannascimento.agendebem.databinding.FragmentAgendamentosBinding
import com.devallannascimento.agendebem.model.Consulta
import com.devallannascimento.agendebem.MainActivity.Companion.TAG
import com.devallannascimento.agendebem.utils.restartFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AgendamentosFragment : Fragment() {

    private var agendamentosBinding: FragmentAgendamentosBinding? = null
    private val binding get() = this.agendamentosBinding!!

    private val consultasAdapter = ConsultasAdapter()

    private val firebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    private val firebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    private val idUsuario = firebaseAuth.currentUser?.uid

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        this.agendamentosBinding = FragmentAgendamentosBinding.inflate(inflater, container, false)

        recuperarConsultas()

        return this.binding.root
    }

    private fun recuperarConsultas() {

        Log.i(TAG, "recuperarConsultas: iniciado ")

        if (idUsuario != null){
            val consultasCollection = firebaseFirestore
                .collection("usuarios")
                .document(idUsuario)
                .collection("consultas")

            consultasCollection.get()
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
                    Log.e(MainActivity.TAG, "Erro ao obter documentos: $exception")
                }
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
        val disponivel = consultaSelecionada.disponivel

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
            .setTitle("Deseja cancelar a consulta?")
            .setMessage("\nConfirmar o cancelamento da consulta de $especialidade para o dia:\n\n$data às $hora?")
            .setPositiveButton("Sim") { dialog, posicao ->
                cancelarConsulta(consulta)
                restartFragment()
            }
            .setNegativeButton("Não") { dialog, posicao ->
                // Lógica para lidar com o não
            }
            .create().show()
    }

    private fun cancelarConsulta(consulta: Consulta) {
        val consultasCollection = firebaseFirestore
            .collection("consultas")

        // Atualizar o campo "disponivel" para "false"
        consultasCollection.document(consulta.id)
            .update("disponivel", "true")
            .addOnSuccessListener {
                firebaseFirestore
                    .collection("usuarios")
                    .document(idUsuario!!)
                    .collection("consultas")
                    .document(consulta.id)
                    .update("disponivel", "false")
                Log.d(TAG, "Consulta cancelada")
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Erro ao atualizar o campo 'disponivel': $exception")
            }
    }
}