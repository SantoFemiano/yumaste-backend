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

    @Column(name = "cf", nullable = false, length = 16)
    private String cf;

    @Column(name = "nome", nullable = false, length = 50)
    private String nome;

    @Column(name = "cognome", nullable = false, length = 50)
    private String cognome;

    @Column(name = "data_nascita", nullable = false)
    private LocalDate dataNascita;

    @Column(name = "telefono", nullable = false, length = 15)
    private String telefono;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password_c", nullable = false)
    private String passwordC;

    @ColumnDefault("'USER'")
    @Column(name = "ruolo", length = 20)
    private String ruolo;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "data_registrazione")
    private Instant dataRegistrazione;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "data_aggiornamento")
    private Instant dataAggiornamento;

    // =========================================================================
    // METODI DELL'INTERFACCIA USERDETAILS DI SPRING SECURITY
    // =========================================================================

    /**
     * Restituisce i ruoli (permessi) concessi all'utente.
     */
    @Override
    @NonNull
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Spring Security si aspetta che i ruoli inizino con "ROLE_".
        // Se nel DB salvi solo "USER" o "ADMIN", qui aggiungiamo il prefisso al volo.
        String rolePrefix = (this.ruolo != null && this.ruolo.startsWith("ROLE_")) ? "" : "ROLE_";
        String authority = rolePrefix + (this.ruolo != null ? this.ruolo : "USER");

        return List.of(new SimpleGrantedAuthority(authority));
    }

    /**
     * Spring Security ha bisogno di sapere qual è la password criptata da confrontare.
     * Mappiamo la colonna "passwordC".
     */
    @Override
    public String getPassword() {
        return this.passwordC;
    }

    /**
     * Spring Security usa "Username" come identificatore univoco.
     * Nel nostro caso, l'identificatore è l'email.
     */
    @Override
    @NonNull
    public String getUsername() {
        return this.email;
    }

    /**
     * Indica se l'account dell'utente è scaduto.
     */
    @Override
    public boolean isAccountNonExpired() {
        return true; // Per ora lo lasciamo sempre a true (non scade mai)
    }

    /**
     * Indica se l'utente è bloccato o meno.
     */
    @Override
    public boolean isAccountNonLocked() {
        return true; // Mettiamo true. Un domani potresti legarlo a una colonna "bloccato" nel DB
    }

    /**
     * Indica se le credenziali (password) dell'utente sono scadute.
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Indica se l'utente è abilitato o disabilitato.
     */
    @Override
    public boolean isEnabled() {
        return true; // Mettiamo true. Potresti legarlo a una verifica email (es. utente confermato)
    }
}