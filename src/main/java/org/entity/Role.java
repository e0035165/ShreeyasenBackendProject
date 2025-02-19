package org.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Role")
@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class Role {

    public Role(String name) {
        this.name=name;
        this.users=new ArrayList<>();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name="name")
    private String name;

    @ManyToMany(mappedBy = "roles")
    private List<CustomUserDetails> users;
}
