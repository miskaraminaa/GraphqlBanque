package com.example.banque_service.controllers;


import com.example.banque_service.entities.*;
import com.example.banque_service.repositories.CompteRepository;
import com.example.banque_service.repositories.TransactionRepository;
import lombok.AllArgsConstructor;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
@AllArgsConstructor
public class CompteControllerGraphQL {

    private final CompteRepository compteRepository;
    private final TransactionRepository transactionRepository;


    @QueryMapping
    public List<Compte> allComptes() {
        return compteRepository.findAll();
    }

    @QueryMapping
    public Compte compteById(@Argument Long id) {
        return compteRepository.findById(id).orElseThrow(
                () -> new RuntimeException(String.format("Compte with id %s not found", id))
        );
    }

    @MutationMapping
    public Compte saveCompte(@Argument Compte compte) {
        return compteRepository.save(compte);
    }


    @QueryMapping
    public Map<String, Object> totalSolde() {
        long count = compteRepository.count();
        double sum = compteRepository.sumSoldes();
        double average = count > 0 ? sum / count : 0;
        return Map.of(
                "count", count,
                "sum", sum,
                "average", average
        );
    }
    @QueryMapping
    public List<Transaction> allTransactions() {
        return transactionRepository.findAll();
    }

    @QueryMapping
    public List<Transaction> compteTransactions(@Argument Long compteId) {
        return transactionRepository.findByCompteId(compteId);
    }


    @QueryMapping
    public Map<String, Object> transactionStats() {
        long totalCount = transactionRepository.countTotalTransactions();
        double sumDepot = transactionRepository.sumDepot() != null
                ? transactionRepository.sumDepot()
                : 0.0;
        double sumRetrait = transactionRepository.sumRetrait() != null
                ? transactionRepository.sumRetrait()
                : 0.0;

        return Map.of(
                "totalCount", totalCount,
                "sumDepot", sumDepot,
                "sumRetrait", sumRetrait
        );
    }
    @MutationMapping
    public Transaction addTransaction(@Argument("transaction") TransactionInput input) {
        if (input == null) {
            throw new RuntimeException("TransactionInput cannot be null");
        }

        Compte compte = compteRepository.findById(input.getCompteId())
                .orElseThrow(() -> new RuntimeException("Compte not found"));

        if (input.getType().equalsIgnoreCase("RETRAIT") && compte.getSolde() < input.getMontant()) {
            throw new RuntimeException("Insufficient balance");
        }
        TypeTransaction transactionType = TypeTransaction.valueOf(input.getType().toUpperCase());
        double newSolde = transactionType == TypeTransaction.DEPOT
                ? compte.getSolde() + input.getMontant()
                : compte.getSolde() - input.getMontant();
        compte.setSolde(newSolde);
        compteRepository.save(compte);

        Transaction transaction = new Transaction();
        transaction.setMontant(input.getMontant());
        transaction.setDate(transaction.getDate());
        transaction.setType(transactionType);
        transaction.setCompte(compte);

        return transactionRepository.save(transaction);
    }




    @MutationMapping
    public String deleteCompte(@Argument Long id) {
        Compte compte = compteRepository.findById(id).orElseThrow(
                () -> new RuntimeException(String.format("Compte with id %s not found", id))
        );
        compteRepository.delete(compte);
        return "Le compte " + id + " est bien supprimé !";
    }

    @QueryMapping
    public List<Compte> compteByType(@Argument TypeCompte type) {
        return compteRepository.findByType(type);
    }
}