package org.alixar.daw2.JoseManuel.dwese_ticket_logger_webapp.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

/**
 * Un almacén tiene una licencia de almacén y contiene varios pasillos.
 * Representa un almacén dentro del sistema.
 */
@Entity
@Table(name = "almacenes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Almacen {

    /**
     * Identificador único del almacén.
     * Se genera automáticamente.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Ciudad en la que se encuentra el almacén.
     * Es obligatoria y tiene una longitud máxima de 100 caracteres.
     */
    @NotEmpty(message = "{msg.almacen.ciudad.notEmpty}")
    @NotNull(message = "{msg.almacen.ciudad.notNull}")
    @NotBlank(message = "{msg.almacen.ciudad.notBlank}")
    @Size(max = 100, message = "{msg.almacen.ciudad.size}")
    @Column(name = "ciudad", nullable = false, length = 100)
    private String ciudad;

    /**
     * Licencia de almacén asociada al almacén.
     * Relación uno a uno (1:1).
     * Es el lado inverso de la relación con LicenciaAlmacen.
     */
    @OneToOne(mappedBy = "almacen")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private LicenciaAlmacen licenciaAlmacen;

    /**
     * Lista de pasillos que contiene el almacén.
     * Relación uno a muchos (1:N).
     * Es el lado inverso de la relación con Pasillo.
     */
    // Pasillos del almacén (1:N). Lado inverso de la relación.
    @OneToMany(mappedBy = "almacen", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Pasillo> pasillos;

    /**
     * Constructor sin `id` (lo genera la base de datos), sin la licencia y sin la lista de pasillos.
     * Se usa para crear un almacén nuevo antes de insertarlo.
     *
     * @param ciudad Ciudad en la que se encuentra el almacén.
     */
    public Almacen(String ciudad) {
        this.ciudad = ciudad;
    }
}
