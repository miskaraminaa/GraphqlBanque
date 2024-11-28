package ma.ensa.banquegraphql

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import ma.ensa.banquegraphql.adapter.ComptesAdapter
import ma.ensa.banquegraphql.data.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import ma.ensa.banquegraphql.type.TypeCompte

class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var addButton: Button
    private lateinit var totalCountText: TextView
    private lateinit var totalSumText: TextView
    private lateinit var averageText: TextView
    private lateinit var statsCard: View
    private lateinit var filterButton: Button
    private lateinit var filterValueInput: EditText
    private lateinit var filterTypeSpinner: Spinner

    private val viewModel: MainViewModel by viewModels()
    private val comptesAdapter = ComptesAdapter()

    // Liste pour stocker tous les comptes (liste complète)
    private var allComptes: List<GetAllComptesQuery.AllCompte> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeViews()
        setupRecyclerView()
        setupAddButton()
        observeViewModel()
        setupDeleteListener()

        filterButton.setOnClickListener {
            applyFilter()
        }
    }

    private fun initializeViews() {
        recyclerView = findViewById(R.id.comptesRecyclerView)
        addButton = findViewById(R.id.addCompteButton)
        totalCountText = findViewById(R.id.totalCountText)
        totalSumText = findViewById(R.id.totalSumText)
        averageText = findViewById(R.id.averageText)
        statsCard = findViewById(R.id.statsCard)
        filterButton = findViewById(R.id.filterButton)
        filterValueInput = findViewById(R.id.filterValueInput)
        filterTypeSpinner = findViewById(R.id.filterTypeSpinner)

        // Setup Spinner for filter types
        val filterTypes = resources.getStringArray(R.array.filter_types)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, filterTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        filterTypeSpinner.adapter = adapter
    }

    private fun setupRecyclerView() {
        recyclerView.apply {
            adapter = comptesAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }
    }

    private fun setupAddButton() {
        addButton.setOnClickListener {
            showAddCompteDialog()
        }
    }

    private fun observeViewModel() {
        viewModel.comptesState.observe(this) { state ->
            when (state) {
                is MainViewModel.UIState.Loading -> {
                    // Show loading UI
                }
                is MainViewModel.UIState.Success -> {
                    // Update the full list of accounts and adapter
                    allComptes = state.data
                    comptesAdapter.updateList(allComptes)
                }
                is MainViewModel.UIState.Error -> {
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
            }
        }

        viewModel.totalSoldeState.observe(this) { state ->
            when (state) {
                is MainViewModel.UIState.Loading -> statsCard.visibility = View.INVISIBLE
                is MainViewModel.UIState.Success -> {
                    statsCard.visibility = View.VISIBLE
                    totalCountText.text = "Total des comptes: ${state.data?.count}"
                    totalSumText.text = "La somme : ${state.data?.sum} MAD"
                    averageText.text = "La moyenne : ${state.data?.average} MAD"
                }
                is MainViewModel.UIState.Error -> {
                    statsCard.visibility = View.INVISIBLE
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun applyFilter() {
        val filterType = filterTypeSpinner.selectedItem.toString()
        val filteredComptes = when (filterType.uppercase()) {
            "COURANT" -> allComptes.filter { it.type == TypeCompte.COURANT }
            "EPARGNE" -> allComptes.filter { it.type == TypeCompte.EPARGNE }
            else -> allComptes // Return all accounts if no specific type is selected
        }

        comptesAdapter.updateList(filteredComptes)
    }

    private fun showAddCompteDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_compte, null)
        val soldeInput = dialogView.findViewById<TextInputEditText>(R.id.soldeInput)
        val typeSpinner = dialogView.findViewById<Spinner>(R.id.type_options)

        val typeAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.type_options,
            android.R.layout.simple_spinner_item
        )
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        typeSpinner.adapter = typeAdapter

        MaterialAlertDialogBuilder(this)
            .setTitle("Nouveau Compte Bancaire")
            .setView(dialogView)
            .setPositiveButton("Ajouter") { _, _ ->
                val solde = soldeInput.text.toString().toDoubleOrNull()
                val selectedType = typeSpinner.selectedItem.toString()
                val type = when (selectedType.uppercase()) {
                    "COURANT" -> TypeCompte.COURANT
                    "EPARGNE" -> TypeCompte.EPARGNE
                    else -> TypeCompte.COURANT
                }

                if (solde != null) {
                    val dateCreation =
                        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                    viewModel.saveCompte(solde, type, dateCreation)
                } else {
                    Toast.makeText(this, "Saisissez un solde valide !", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    private fun setupDeleteListener() {
        comptesAdapter.setOnDeleteClickListener { compteId ->
            MaterialAlertDialogBuilder(this)
                .setTitle("Supprimer le compte")
                .setMessage("Êtes-vous sûr de vouloir supprimer ce compte ?")
                .setPositiveButton("Oui") { _, _ ->
                    viewModel.deleteCompte(compteId)
                }
                .setNegativeButton("Annuler", null)
                .show()
        }
    }
}
