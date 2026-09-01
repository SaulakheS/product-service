package com.zestindia.productservice.repository;

import com.zestindia.productservice.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

    Page<Product> findByProductNameContainingIgnoreCase(String productName, Pageable pageable);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.items WHERE p.id = :id")
    Optional<Product> findByIdWithItems(@Param("id") Integer id);

    boolean existsByProductNameIgnoreCase(String productName);
}
