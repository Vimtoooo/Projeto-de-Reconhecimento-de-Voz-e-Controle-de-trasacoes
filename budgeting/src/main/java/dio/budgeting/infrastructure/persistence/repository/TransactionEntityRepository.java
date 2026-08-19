package dio.budgeting.infrastructure.persistence.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.repository.CrudRepository;

import dio.budgeting.domain.Category;
import dio.budgeting.infrastructure.persistence.entity.TransactionEntity;

public interface TransactionEntityRepository extends CrudRepository<TransactionEntity, UUID> {
    List<TransactionEntity> findAllByCategory(Category category);
}