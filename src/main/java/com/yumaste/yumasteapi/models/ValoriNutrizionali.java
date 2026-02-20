package com.yumaste.yumasteapi.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import java.math.BigDecimal;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "VALORI_NUTRIZIONALI")
public class ValoriNutrizionali {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "ingrediente_id", nullable = false)
    private Ingrediente ingrediente;

    @Column(name = "proteine", nullable = false, precision = 5, scale = 2)
    private BigDecimal proteine;

    @Column(name = "carboidrati", nullable = false, precision = 5, scale = 2)
    private BigDecimal carboidrati;

    @Column(name = "zuccheri", nullable = false, precision = 5, scale = 2)
    private BigDecimal zuccheri;

    @Column(name = "fibre", nullable = false, precision = 5, scale = 2)
    private BigDecimal fibre;

    @Column(name = "grassi", nullable = false, precision = 5, scale = 2)
    private BigDecimal grassi;

    @Column(name = "sale", nullable = false, precision = 5, scale = 2)
    private BigDecimal sale;

    @Column(name = "chilocalorie", nullable = false)
    private Integer chilocalorie;


}