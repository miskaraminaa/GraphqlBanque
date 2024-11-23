package com.example.banque_service.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double montant;

    @Enumerated(EnumType.STRING)
    private TypeTransaction type; // DEPOT ou RETRAIT

    private LocalDateTime date;

    @ManyToOne
    @JoinColumn(name = "compte_id") // Clé étrangère vers l'entité Compte
    private Compte compte;
}
