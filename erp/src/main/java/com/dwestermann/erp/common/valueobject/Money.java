package com.dwestermann.erp.common.valueobject;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Objects;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Money {

    private BigDecimal amount;
    private String currency;

    public Money(BigDecimal amount) {
        this(amount, "EUR");
    }

    public Money subtract(Money other) {
        if (!Objects.equals(this.currency, other.currency)) {
            throw new IllegalArgumentException("Cannot subtract different currencies");
        }
        return new Money(this.amount.subtract(other.amount), this.currency);
    }

    public Money add(Money other) {
        if (!Objects.equals(this.currency, other.currency)) {
            throw new IllegalArgumentException("Cannot add different currencies");
        }
        return new Money(this.amount.add(other.amount), this.currency);
    }

    public Money multiply(BigDecimal factor) {
        return new Money(this.amount.multiply(factor), this.currency);
    }

    public boolean isZero() {
        return amount == null || amount.compareTo(BigDecimal.ZERO) == 0;
    }

    public boolean isPositive() {
        return amount != null && amount.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isNegative() {
        return amount != null && amount.compareTo(BigDecimal.ZERO) < 0;
    }

    @Override
    public String toString() {
        return String.format("%s %s", currency, amount);
    }
}