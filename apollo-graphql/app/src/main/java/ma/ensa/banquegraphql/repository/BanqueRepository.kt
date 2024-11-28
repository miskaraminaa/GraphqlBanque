package ma.ensa.banquegraphql.repository

import android.util.Log
import com.apollographql.apollo3.ApolloClient
import ma.ensa.banquegraphql.GetAllComptesQuery
import ma.ensa.banquegraphql.GetTotalSoldeQuery
import ma.ensa.banquegraphql.SaveCompteMutation
import ma.ensa.banquegraphql.type.CompteInput
import ma.ensa.banquegraphql.type.TypeCompte


class BanqueRepository {
    private val TAG = "BanqueRepository"
    private val apolloClient = ApolloClient.Builder()
        .serverUrl("http://10.0.2.2:8082/graphql")
        .build()


    suspend fun getAllComptes(callback: (Result<List<GetAllComptesQuery.AllCompte>>) -> Unit) {
        try {
            Log.d(TAG, "Fetching all comptes...")
            val response = apolloClient.query(GetAllComptesQuery()).execute()
            if (response.hasErrors()) {
                val error = response.errors?.first()?.message ?: "Unknown error"
                Log.e(TAG, "Error fetching comptes: $error")
                callback(Result.failure(Exception(error)))
            } else {
                val comptes = response.data?.allComptes?.filterNotNull() ?: emptyList()
                Log.d(TAG, "Fetched ${comptes.size} comptes")
                callback(Result.success(comptes))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception while fetching comptes", e)
            callback(Result.failure(e))
        }
    }

    suspend fun getTotalSolde(callback: (Result<GetTotalSoldeQuery.TotalSolde>) -> Unit) {
        try {
            Log.d(TAG, "Fetching total solde...")
            val response = apolloClient.query(GetTotalSoldeQuery()).execute()
            if (response.hasErrors()) {
                val error = response.errors?.first()?.message ?: "Unknown error"
                Log.e(TAG, "Error fetching total solde: $error")
                callback(Result.failure(Exception(error)))
            } else {
                response.data?.totalSolde?.let { totalSolde ->
                    Log.d(TAG, "Successfully fetched total solde")
                    callback(Result.success(totalSolde))
                } ?: callback(Result.failure(Exception("Total solde response was null")))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception while fetching total solde", e)
            callback(Result.failure(e))
        }
    }

    // Modification du code pour envelopper solde, type, et dateCreation dans Optional.Present
    suspend fun saveCompte(
        solde: Double,
        type: TypeCompte,
        dateCreation: String,
        callback: (Result<SaveCompteMutation.SaveCompte>) -> Unit
    ) {
        try {
            Log.d(TAG, "Saving compte with solde: $solde, type: $type, and dateCreation: $dateCreation")

            // Créer un objet CompteInput et envelopper les champs dans Optional.Present
            val input = CompteInput(
                solde = solde, // pas besoin d'envelopper solde car c'est un type primitif
                type = type, // Enveloppez le type dans Optional.Present
                dateCreation = dateCreation // Enveloppez dateCreation dans Optional.Present
            )

            // Exécutez la mutation
            val response = apolloClient.mutation(SaveCompteMutation(input)).execute()

            if (response.hasErrors()) {
                val error = response.errors?.first()?.message ?: "Unknown error"
                Log.e(TAG, "Error saving compte: $error")
                callback(Result.failure(Exception(error)))
            } else {
                response.data?.saveCompte?.let { savedCompte ->
                    Log.d(TAG, "Compte saved successfully with id: ${savedCompte.id}")
                    callback(Result.success(savedCompte))
                } ?: callback(Result.failure(Exception("Save response was null")))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception while saving compte", e)
            callback(Result.failure(e))
        }
    }


}