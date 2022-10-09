package com.example.login.model.persistance;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "registration_token")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class RegistrationToken {

    @Id
    @SequenceGenerator(name = "registration_token_id_seq", sequenceName = "registration_token_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "registration_token_id_seq")
    private Long id;

    @Column(name = "token")
    private String token;

    @Column(name = "creation_date")
    private LocalDateTime creationDate;

    @Column(name = "expiration_date")
    private LocalDateTime expirationDate;

    @ManyToOne
    @JoinColumn(name = "app_user_id")
    private SystemUser systemUser;
}
