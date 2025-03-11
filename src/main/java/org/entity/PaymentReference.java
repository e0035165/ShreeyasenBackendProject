package org.entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Payment_records")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class PaymentReference {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name="client_id", length = 65535)
    private String client_id;

    @Column(name="client_secret", length = 65535)
    private String client_secret;

}
