package org.alixar.daw2.JoseManuel.dwese_ticket_logger_webapp.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

/**
 * Representa un pasillo dentro de un almacén.
 * Un pasillo pertenece a un almacén y puede contener varias estanterías.
 */
@Entity
@Table(name = "pasillos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pasillo {

    /**
     * Identificador único del pasillo.
     * Se genera automáticamente.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Número identificativo del pasillo.
     * Es obligatorio, debe ser como mínimo 1 y no puede repetirse.
     */
    // Número del pasillo (único en toda la tabla).
    @NotNull(message = "{msg.pasillo.numero.notNull}")
    @Min(value = 1, message = "{msg.pasillo.numero.min}")
    @Column(name = "numero", nullable = false, unique = true)
    private Integer numero;

    /**
     * Almacén al que pertenece el pasillo.
     * Relación muchos a uno (N:1).
     * No puede ser nulo.
     */
    // Almacén al que pertenece el pasillo (N:1).
    @NotNull(message = "{msg.pasillo.almacen.notNull}")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "almacen_id", nullable = false)
    private Almacen almacen;

    /**
     * Lista de estanterías que contiene el pasillo.
     * Relación uno a muchos (1:N).
     * Es el lado inverso de la relación con Estanteria.
     */
    // Estanterías del pasillo (1:N). Lado inverso de la relación.
    @OneToMany(mappedBy = "pasillo", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Estanteria> estanterias;

    /**
     * Constructor que permite crear un pasillo indicando su número
     * y el almacén al que pertenece.
     *
     * @param numero número identificativo del pasillo
     * @param almacen almacén al que pertenece el pasillo
     */
    public Pasillo(Integer numero, Almacen almacen) {
        this.numero = numero;
        this.almacen = almacen;
    }
}