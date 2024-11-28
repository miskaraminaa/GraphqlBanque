package ma.ensa.banquegraphql.data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ma.ensa.banquegraphql.GetAllComptesQuery
import ma.ensa.banquegraphql.GetTotalSoldeQuery
import ma.ensa.banquegraphql.SaveCompteMutation
import ma.ensa.banquegraphql.repository.BanqueRepository
import ma.ensa.banquegraphql.type.TypeCompte

class MainViewModel : ViewModel() {
    private val TAG = "MainViewModel"
    private val repository = BanqueRepository()
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    // State for accounts
    private val _comptesState = MutableLiveData<UIState<List<GetAllComptesQuery.AllCompte>>>()
    val comptesState: LiveData<UIState<List<GetAllComptesQuery.AllCompte>>> = _comptesState

    // State for total balance
    private val _totalSoldeState = MutableLiveData<UIState<GetTotalSoldeQuery.TotalSolde?>>()
    val totalSoldeState: LiveData<UIState<GetTotalSoldeQuery.TotalSolde?>> = _totalSoldeState

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _success = MutableLiveData<String>()
    val success: LiveData<String> get() = _success

    init {
        loadComptes()
        loadTotalSolde()
    }


    fun loadComptes(type: TypeCompte? = null) {
        coroutineScope.launch(Dispatchers.IO) {
            _comptesState.postValue(UIState.Loading)
            try {
                repository.getAllComptes { result ->
                    result.fold(
                        onSuccess = { comptesList ->
                            val filteredComptes = if (type != null) {
                                comptesList.filter { it.type == type }
                            } else {
                                comptesList
                            }
                            _comptesState.postValue(UIState.Success(filteredComptes))
                            Log.d(TAG, "Successfully fetched ${filteredComptes.size} comptes")
                        },
                        onFailure = { exception ->
                            _comptesState.postValue(UIState.Error(exception.message ?: "Unknown error"))
                            Log.e(TAG, "Error fetching comptes", exception)
                        }
                    )
                }
            } catch (e: Exception) {
                _comptesState.postValue(UIState.Error(e.message ?: "Unknown error"))
                Log.e(TAG, "Exception while loading comptes", e)
            }
        }
    }


    // Load total balance
    fun loadTotalSolde() {
        coroutineScope.launch(Dispatchers.IO) {
            _totalSoldeState.postValue(UIState.Loading)
            try {
                repository.getTotalSolde { result ->
                    result.fold(
                        onSuccess = { totalSolde ->
                            _totalSoldeState.postValue(UIState.Success(totalSolde))
                            Log.d(TAG, "Successfully fetched total solde")
                        },
                        onFailure = { exception ->
                            _totalSoldeState.postValue(UIState.Error(exception.message ?: "Unknown error"))
                            Log.e(TAG, "Error fetching total solde", exception)
                        }
                    )
                }
            } catch (e: Exception) {
                _totalSoldeState.postValue(UIState.Error(e.message ?: "Unknown error"))
                Log.e(TAG, "Exception while loading total solde", e)
            }
        }
    }

    // Save a new account
    fun saveCompte(solde: Double, type: TypeCompte, dateCreation: String) {
        coroutineScope.launch(Dispatchers.IO) {
            _comptesState.postValue(UIState.Loading)
            try {
                repository.saveCompte(solde, type, dateCreation) { result ->
                    result.fold(
                        onSuccess = { savedCompte ->
                            // Vous devez adapter le type SaveCompteMutation.SaveCompte à GetAllComptesQuery.AllCompte
                            // Assurez-vous que les propriétés sont compatibles ou mappez-les si nécessaire.
                            val allCompte = mapSaveCompteToAllCompte(savedCompte)

                            // Append the saved compte to the current list of comptes
                            _comptesState.value?.let { currentState ->
                                if (currentState is UIState.Success) {
                                    val updatedList = currentState.data.toMutableList()
                                    updatedList.add(allCompte)
                                    _comptesState.postValue(UIState.Success(updatedList))
                                }
                            }
                            _success.postValue("Compte added successfully")
                        },
                        onFailure = { error ->
                            _comptesState.postValue(UIState.Error(error.message ?: "Unknown error"))
                            _error.postValue("Error adding compte")
                        }
                    )
                }
            } catch (e: Exception) {
                _comptesState.postValue(UIState.Error("Exception: ${e.message}"))
            }
        }
    }

    // Helper function to map SaveCompteMutation.SaveCompte to GetAllComptesQuery.AllCompte
    private fun mapSaveCompteToAllCompte(savedCompte: SaveCompteMutation.SaveCompte): GetAllComptesQuery.AllCompte {
        return GetAllComptesQuery.AllCompte(
            id = savedCompte.id,
            solde = savedCompte.solde,
            type = savedCompte.type,
            dateCreation = savedCompte.dateCreation
        )
    }


    override fun onCleared() {
        super.onCleared()
        coroutineScope.launch {
            // Clean up resources if needed
        }
    }

    // Sealed class for UI state
    sealed class UIState<out T> {
        object Loading : UIState<Nothing>()
        data class Success<T>(val data: T) : UIState<T>()
        data class Error(val message: String) : UIState<Nothing>()
    }

}
