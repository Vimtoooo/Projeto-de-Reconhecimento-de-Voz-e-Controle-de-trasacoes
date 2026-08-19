package dio.budgeting.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Transaction {
    public TransactionId id;
    public String description;
    private long amount;
    private Category category;

    public Transaction(String description, long amount, Category category) {
        this.id = new TransactionId();
        this.description = description;
        this.amount = amount;
        this.category = category;
    }

    public TransactionId getId() {
        return id;
    }


    public String getDescription() {
        return description;
    }


    public long getAmount() {
        return amount;
    }


    public Category getCategory() {
        return category;
    }
}
