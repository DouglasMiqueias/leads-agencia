package br.com.agencia.leads.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "usuarios")
@Getter @Setter
public class UserAccount {
    public enum Role { ADMIN, USER }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String nome;
    @Column(nullable = false, unique = true)
    private String email;
    @Column(nullable = false)
    private String senhaHash;
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private Role role = Role.USER;
    private boolean ativo = true;
}
