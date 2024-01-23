import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.devallannascimento.agendebem.databinding.ItemConsultaBinding
import com.devallannascimento.agendebem.model.Consulta
import com.devallannascimento.agendebem.MainActivity.Companion.TAG

class ConsultasAdapter : RecyclerView.Adapter<ConsultasAdapter.ConsultaViewHolder>() {

    private val consultasList = mutableListOf<Consulta>()

    fun atualizarConsultas(consultas: List<Consulta>) {
        consultasList.clear()
        consultasList.addAll(consultas)
        notifyDataSetChanged()
    }

    inner class ConsultaViewHolder(private val binding: ItemConsultaBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(consulta: Consulta) {
            // Vincule os dados aos elementos de layout aqui
            binding.textDoutor.text = consulta.medico
            binding.textCRM.text = "${consulta.crm} - ${consulta.especialidade}"
            binding.textValor.text = "R$ ${consulta.valor}"
            binding.textDataEHora.text = "${consulta.data} às ${consulta.hora}"
            // Adicione outros vínculos conforme necessário
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConsultaViewHolder {

        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemConsultaBinding.inflate(
            layoutInflater,parent,false
        )
        return ConsultaViewHolder(binding)

    }

    override fun onBindViewHolder(holder: ConsultaViewHolder, position: Int) {
        val consulta = consultasList[position]
        holder.bind(consulta)
    }

    // Adicione um método para obter uma consulta com base na posição
    fun getConsulta(position: Int): Consulta {
        return consultasList[position]
    }

    override fun getItemCount(): Int {
        return consultasList.size
    }
}
