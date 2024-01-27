package com.devallannascimento.agendebem.fragments

import ConsultasAdapter
//noinspection SuspiciousImport
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

    private val especialidadesList = mutableListOf<String>()

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

                binding.spinnerEspecialidades.onItemSelectedListener =
                    object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            val especialidadeSelecionada = especialidadesList[position]
                            recuperarConsultas(especialidadeSelecionada)
                        }

                        override fun onNothingSelected(parent: AdapterView<*>?) {}
                    }

            }.addOnFailureListener { exception ->
                Log.e(TAG, "Erro ao obter documentos: $exception")
            }
    }

    private fun recuperarConsultas(especialidade: String) {

        val consultasCollection = FirebaseFirestore.getInstance().collection("consultas")
        val consultaQuery = consultasCollection.whereEqualTo("especialidade", especialidade)
        consultaQuery.get()
            .addOnSuccessListener { querySnapshot ->
                val consultasList = mutableListOf<Consulta>()

                querySnapshot.documents.forEach { document ->

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
            .addOnFailureListener { exception ->
                Log.e(TAG, "Erro ao obter documentos: $exception")
            }
    }

    private fun inicializarRecyclerView(consultasList: List<Consulta>) {
        val recyclerView: RecyclerView = binding.recyclerViewConsultas
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
                atualizarDisponibilidadeConsulta(consultaSelecionada)
                criarDocumentoUsuario(consulta)
            }
            .setNegativeButton("Não") { _, _ -> }
            .create().show()
    }

    private fun atualizarDisponibilidadeConsulta(consulta: Consulta) {

        firebaseFirestore
            .collection("consultas")
            .document(consulta.id)
            .update("disponivel", "false")

    }

    private fun criarDocumentoUsuario(consulta: Consulta) {

        firebaseFirestore
            .collection("usuarios")
            .document(idUsuario!!)
            .collection("consultas")
            .document(consulta.id)
            .set(consulta)
            .addOnSuccessListener {
                exibirMensagem("Sucesso ao marcar consulta")
                recuperarConsultas(binding.spinnerEspecialidades.selectedItem.toString())
            }
            .addOnFailureListener {
                exibirMensagem("Erro ao marcar consulta")
            }

    }
}