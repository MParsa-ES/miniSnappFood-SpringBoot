package com.parsa.miniSnappFood.repository;

import com.parsa.miniSnappFood.model.FoodItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FoodItemRepository extends JpaRepository<FoodItem, Long> {

    List<FoodItem> findByCategoryId(Long categoryId);
}
