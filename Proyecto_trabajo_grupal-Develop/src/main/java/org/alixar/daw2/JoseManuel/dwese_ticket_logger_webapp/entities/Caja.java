package org.alixar.daw2.JoseManuel.dwese_ticket_logger_webapp.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Una caja se guarda en una estantería y puede tener una etiqueta de producto.
 * Representa una caja dentro del almacén.
 */
@Entity
@Table(name = "cajas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Caja {

    /**
     * Identificador único de la caja.
     * Se genera automáticamente.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Peso de la caja en kilogramos.
     * Es obligatorio y debe ser mayor que 0.
     * Se usa Double (y no double) para detectar el campo vacío del formulario.
     */
    @NotNull(message = "{msg.caja.peso.notNull}")
    @Positive(message = "{msg.caja.peso.positive}")
    @Column(name = "peso", nullable = false)
    private Double peso;

    /**
     * Descripción de lo que contiene la caja.
     * Es obligatorio y tiene una longitud máxima de 255 caracteres.
     */
    @NotBlank(message = "{msg.caja.contenido.notBlank}")
    @Size(max = 255, message = "{msg.caja.contenido.size}")
    @Column(name = "contenido", nullable = false, length = 255)
    private String contenido;

    /**
     * Estantería en la que está colocada la caja.
     * Relación muchos a uno (N:1).
     * No puede ser nulo.
     */
    @NotNull(message = "{msg.caja.estanteria.notNull}")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estanteria_id", nullable = false)
    private Estanteria estanteria;

    /**
     * Etiqueta de producto asociada a la caja.
     * Relación uno a uno (1:1).
     * Es el lado inverso de la relación con EtiquetaProducto.
     */
    @OneToOne(mappedBy = "caja")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private EtiquetaProducto etiquetaProducto;

    /**
     * Constructor sin `id` (lo genera la base de datos) y sin la etiqueta.
     * Se usa para crear una caja nueva antes de insertarla.
     *
     * @param peso       Peso de la caja en kilogramos.
     * @param contenido  Contenido de la caja.
     * @param estanteria Estantería en la que está colocada.
     */
    public Caja(Double peso, String contenido, Estanteria estanteria) {
        this.peso = peso;
        this.contenido = contenido;
        this.estanteria = estanteria;
    }
}
