package ma.ensa.banquegraphql.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ma.ensa.banquegraphql.GetAllComptesQuery
import ma.ensa.banquegraphql.R

class ComptesAdapter : RecyclerView.Adapter<ComptesAdapter.CompteViewHolder>() {

    // List to hold account data
    private var comptes: List<GetAllComptesQuery.AllCompte> = listOf()

    // Listener for delete click
    private var deleteClickListener: ((String) -> Unit)? = null

    // Function to set the delete click listener
    fun setOnDeleteClickListener(listener: (String) -> Unit) {
        deleteClickListener = listener
    }

    // Function to update the list of accounts
    fun updateList(newList: List<GetAllComptesQuery.AllCompte>) {
        comptes = newList
        notifyDataSetChanged()
    }

    // Function to retrieve all accounts
    fun getAllComptes(): List<GetAllComptesQuery.AllCompte> {
        return comptes
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_compte, parent, false)
        return CompteViewHolder(view)
    }

    override fun onBindViewHolder(holder: CompteViewHolder, position: Int) {
        val compte = comptes[position]
        holder.bind(compte)

        // Set up delete click listener
        holder.deleteIcon.setOnClickListener {
            deleteClickListener?.invoke(compte.id) // Invoke the listener with the account ID
        }
    }

    override fun getItemCount() = comptes.size

    // ViewHolder for accounts
    class CompteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val accountIdText: TextView = view.findViewById(R.id.accountIdText)
        private val soldeText: TextView = view.findViewById(R.id.soldeText)
        private val typeText: TextView = view.findViewById(R.id.typeText)
        private val dateText: TextView = view.findViewById(R.id.dateText)
        val deleteIcon: ImageView = view.findViewById(R.id.deleteIcon) // Reference to delete icon

        fun bind(compte: GetAllComptesQuery.AllCompte) {
            accountIdText.text = "ID Compte: ${compte.id}"
            soldeText.text = "Solde: ${compte.solde} MAD"
            typeText.text = "Type: ${compte.type}"
            dateText.text = "Créé le: ${compte.dateCreation}"
        }
    }
}
