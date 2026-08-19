package dio.budgeting.application.input;

import org.springframework.ai.tool.annotation.Tool;

import dio.budgeting.domain.Category;

public record PersistTransactionInput(
    @Tool(description = "Descrição do gasto de uma certa transação") String description,
    @Tool(description = "Valor do gasto de uma transação (em centavos)") long amount,
    @Tool(description = "Categoria de uma transação, como exemplo 'GROCERIES'") Category category
) {

    public PersistTransactionInput(String description2, Category category2, long amount2) {
        this(description2, amount2, category2);
    }
}
