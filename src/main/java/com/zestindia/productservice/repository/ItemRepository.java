package com.zestindia.productservice.repository;

import com.zestindia.productservice.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Integer> {

    List<Item> findByProductId(Integer productId);

    void deleteByProductId(Integer productId);
}
