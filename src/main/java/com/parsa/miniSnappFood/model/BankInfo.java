package com.parsa.miniSnappFood.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BankInfo {

    private String bankName;
    private String accountNumber;
}
