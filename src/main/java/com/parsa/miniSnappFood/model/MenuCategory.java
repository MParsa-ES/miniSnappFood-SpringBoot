package com.parsa.miniSnappFood.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "menu_categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MenuCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id",  nullable = false)
    private Restaurant restaurant;

    @Column(nullable = false)
    private String name;

}
