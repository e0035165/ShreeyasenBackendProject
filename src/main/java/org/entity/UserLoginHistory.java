package org.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;

@Entity
@Table(name="User_Login_History")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class UserLoginHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private long id;

    @Column(name="user_email")
    private String email;


    @Column(name="timeStamp")
    private Timestamp timestamp;




}
