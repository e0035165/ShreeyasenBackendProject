package org.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "CONTACTS")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class Contacts {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Integer id;

    @Column(name="number")
    private String number;

    @Column(name="type")
    private String type;

    @Column(name="address")
    private String address;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "username", referencedColumnName = "username")
    private CustomUserDetails userDetails;

}
