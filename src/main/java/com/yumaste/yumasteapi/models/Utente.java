package com.yumaste.yumasteapi.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "UTENTE")
public class Utente implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    // Modificato in nullable = true per accogliere utenti OAuth
    @Column(name = "cf", nullable = true, length = 16)
    private String cf;

    @Column(name = "nome", nullable = false, length = 50)
    private String nome;

    @Column(name = "cognome", nullable = false, length = 50)
    private String cognome;

    // Modificato in nullable = true
    @Column(name = "data_nascita", nullable = true)
    private LocalDate dataNascita;

    // Modificato in nullable = true
    @Column(name = "telefono", nullable = true, length = 15)
    private String telefono;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    // Modificato in nullable = true (gli utenti OAuth non hanno una password locale)
    @Column(name = "password_c", nullable = true)
    private String passwordC;

    @ColumnDefault("'USER'")
    @Column(name = "ruolo", length = 20)
    private String ruolo;

    // Nuovo campo per tracciare il tipo di login (es. "LOCAL", "GITHUB")
    @ColumnDefault("'LOCAL'")
    @Column(name = "provider", nullable = false, length = 20)
    private String provider = "LOCAL";

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "data_registrazione")
    private Instant dataRegistrazione;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "data_aggiornamento")
    private Instant dataAggiornamento;

    @Override
    @NonNull
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String rolePrefix = (this.ruolo != null && this.ruolo.startsWith("ROLE_")) ? "" : "ROLE_";
        String authority = rolePrefix + (this.ruolo != null ? this.ruolo : "USER");
        return List.of(new SimpleGrantedAuthority(authority));
    }

    @Override
    public String getPassword() {
        return this.passwordC;
    }

    @Override
    @NonNull
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}