package com.example.banque_service.entities;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TransactionInput {
    private double montant;
    private String date;
    private String type;
    private Long compteId;

}