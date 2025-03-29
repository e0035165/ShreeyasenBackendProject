package org.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Role")
@Data
@AllArgsConstructor
@RequiredArgsConstructor
@Getter
@Setter
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

    @ManyToMany(mappedBy = "roles", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<CustomUserDetails> users;
}
