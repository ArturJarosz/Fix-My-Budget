package com.arturjarosz.fixmybudget.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@SequenceGenerator(name = "bank_transaction_sequence_generator", sequenceName = "bank_transaction_sequence",
        allocationSize = 1)
@Table(name = "BANK_TRANSACTION")
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class BankTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "bank_transaction_sequence_generator")
    private Long id;

    @EqualsAndHashCode.Include
    @Column(name = "TRANSACTION_DATE", nullable = false)
    private LocalDate transactionDate;

    @EqualsAndHashCode.Include
    @Enumerated(EnumType.STRING)
    @Column(name = "TRANSACTION_TYPE", nullable = false)
    private TransactionType transactionType;

    @EqualsAndHashCode.Include
    @Column(name = "AMOUNT", precision = 5, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "CATEGORY")
    private String category;

    @Column(name = "CATEGORY_OVERRIDDEN")
    private Boolean categoryOverridden;

    @EqualsAndHashCode.Include
    @Column(name = "DETAILS")
    private String details;

    @EqualsAndHashCode.Include
    @Column(name = "RECIPIENT_NAME")
    private String recipientName;

    @EqualsAndHashCode.Include
    @Column(name = "SENDER_NAME")
    private String senderName;

    @EqualsAndHashCode.Include
    @Column(name = "SENDER_ACCOUNT")
    private String senderAccount;

    @EqualsAndHashCode.Include
    @Column(name = "RECIPIENT_ACCOUNT")
    private String recipientAccount;

    @EqualsAndHashCode.Include
    @Column(name = "SOURCE")
    private String source;

    @Column(name = "TRANSACTION_HASH", length = 64, nullable = false)
    private String transactionHash;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof BankTransaction that)) return false;
        return Objects.equals(transactionHash, that.transactionHash);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(transactionHash);
    }

    public String generateDbHashCode() {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String data = String.format("%s|%s|%s|%s|%s|%s|%s|%s",
                    transactionDate,
                    transactionType,
                    amount,
                    senderAccount != null ? senderAccount : "",
                    senderName != null ? senderName : "",
                    recipientAccount != null ? recipientAccount : "",
                    recipientName != null ? recipientName : "",
                    source
            );
            byte[] hashBytes = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to compute hash", e);
        }
    }
}
