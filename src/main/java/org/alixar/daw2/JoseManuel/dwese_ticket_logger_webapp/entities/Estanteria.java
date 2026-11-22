package org.alixar.daw2.JoseManuel.dwese_ticket_logger_webapp.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

/**
 * Una estantería pertenece a un pasillo y contiene varias cajas.
 * Representa una estantería dentro del almacén.
 */
@Entity
@Table(name = "estanterias")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Estanteria {

    /**
     * Identificador único de la estantería.
     * Se genera automáticamente.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Material del que está fabricada la estantería.
     * Es obligatorio y tiene una longitud máxima de 100 caracteres.
     */
    @NotEmpty(message = "{msg.estanteria.material.notEmpty}")
    @Size(max = 100, message = "{msg.estanteria.material.size}")
    @Column(name = "material", nullable = false, length = 100)
    private String material;

    /**
     * Indica si la estantería está llena.
     * True si está llena y false en caso contrario.
     */
    // true si la estantería está llena.
    @Column(name = "lleno", nullable = false)
    private boolean lleno;

    /**
     * Pasillo al que pertenece la estantería.
     * Relación muchos a uno (N:1).
     * No puede ser nulo.
     */
    // Pasillo al que pertenece la estantería (N:1).
    @NotNull(message = "{msg.estanteria.pasillo.notNull}")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pasillo_id", nullable = false)
    private Pasillo pasillo;

    /**
     * Lista de cajas que contiene la estantería.
     * Relación uno a muchos (1:N).
     * Es el lado inverso de la relación con Caja.
     */
    // Cajas de la estantería (1:N). Lado inverso de la relación.
    @OneToMany(mappedBy = "estanteria", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Caja> cajas;

    /**
     * Constructor sin `id` (lo genera la base de datos) y sin la lista de cajas.
     * Se usa para crear una estantería nueva antes de insertarla.
     *
     * @param material Material de la estantería.
     * @param lleno    Si la estantería está llena.
     * @param pasillo  Pasillo al que pertenece.
     */
    public Estanteria(String material, boolean lleno, Pasillo pasillo) {
        this.material = material;
        this.lleno = lleno;
        this.pasillo = pasillo;
    }
}