package com.example.FoodHub.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "role")
public class Role {
    @Id
    @Size(max = 50)
    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @ManyToMany
    @JoinTable(name = "role_permission",
            joinColumns = @JoinColumn(name = "role_name"),
            inverseJoinColumns = @JoinColumn(name = "permission_name"))
    private Set<Permission> permissions = new LinkedHashSet<>();

    @OneToMany(mappedBy = "roleName")
    private Set<User> users = new LinkedHashSet<>();

    @Size(max = 100)
    @Column(name = "description", length = 100)
    private String description;

}