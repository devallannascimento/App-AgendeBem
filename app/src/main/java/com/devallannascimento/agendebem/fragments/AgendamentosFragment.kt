package com.devallannascimento.agendebem.fragments

import ConsultasAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devallannascimento.agendebem.databinding.FragmentAgendamentosBinding
import com.devallannascimento.agendebem.model.Consulta
import com.devallannascimento.agendebem.utils.exibirMensagem
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
    ): View {
        this.agendamentosBinding = FragmentAgendamentosBinding.inflate(inflater, container, false)

        recuperarConsultas()

        return this.binding.root
    }

    private fun recuperarConsultas() {

        if (idUsuario != null) {
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
                    inicializarRecyclerView(consultasList)
                }
        }

    }

    private fun inicializarRecyclerView(consultasList: List<Consulta>) {

        val recyclerView: RecyclerView = binding.recyclerViewConsultas
        if (consultasList.isEmpty()) {
            binding.textHistoricoVazio.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            val layoutManager = LinearLayoutManager(requireContext())
            recyclerView.layoutManager = layoutManager
            recyclerView.adapter = consultasAdapter
            consultasAdapter.atualizarConsultas(consultasList)

            recyclerView.addOnItemTouchListener(
                object : RecyclerView.OnItemTouchListener {
                    override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}

                    override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                        val child = rv.findChildViewUnder(e.x, e.y)
                        if (child != null && e.action == MotionEvent.ACTION_UP) {
                            val position = rv.getChildAdapterPosition(child)
                            exibirDialogoConfirmacao(position)
                        }
                        return false
                    }

                    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
                }
            )
        }

    }

    private fun exibirDialogoConfirmacao(position: Int) {

        val consultaSelecionada = consultasAdapter.getConsulta(position)
        val id = consultaSelecionada.id
        val especialidade = consultaSelecionada.especialidade
        val medico = consultaSelecionada.medico
        val crm = consultaSelecionada.crm
        val data = consultaSelecionada.data
        val hora = consultaSelecionada.hora
        val valor = consultaSelecionada.valor
        consultaSelecionada.disponivel

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
            .setPositiveButton("Sim") { _, _ ->
                recuperarConsultas()
                cancelarConsulta(consulta)
            }
            .setNegativeButton("Não") { _, _ ->
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

                recuperarConsultas()
                exibirMensagem("Consulta cancelada")
            }
    }
}