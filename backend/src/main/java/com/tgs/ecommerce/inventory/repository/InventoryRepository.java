package com.tgs.ecommerce.inventory.repository;

import com.tgs.ecommerce.inventory.domain.Inventory;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findByProductId(Long productId);
}
