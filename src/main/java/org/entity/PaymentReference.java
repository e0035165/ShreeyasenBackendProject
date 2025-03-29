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

    @Column(name="token", length = 65535)
    private String token;

    @Column(name="payer_id", length = 65535)
    private String payer_id;

}
