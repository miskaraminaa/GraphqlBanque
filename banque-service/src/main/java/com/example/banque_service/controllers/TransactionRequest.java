package com.example.banque_service.controllers;

import com.example.banque_service.entities.TypeTransaction;
import lombok.Data;

@Data
public class TransactionRequest {
    private Long compteId;
    private double montant;
    private TypeTransaction type;
}
