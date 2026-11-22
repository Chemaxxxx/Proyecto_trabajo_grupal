package org.alixar.daw2.JoseManuel.dwese_ticket_logger_webapp.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * Una licencia de almacén identifica a un único almacén mediante su fecha
 * de matriculación y su imagen. Representa la licencia asignada a un almacén.
 */
@Entity
@Table(name = "licencias_almacen")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LicenciaAlmacen {

    /**
     * Identificador único de la licencia de almacén.
     * Se genera automáticamente.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Día de matriculación de la licencia del almacén.
     * Es obligatorio y debe ser una fecha pasada o presente.
     */
    @NotNull(message = "{msg.licencia.diaMatriculacion.notNull}")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @PastOrPresent(message = "{msg.licencia.diaMatriculacion.pastOrPresent}")
    @Column(name = "dia_matriculacion", nullable = false)
    private LocalDate diaMatriculacion;

    /**
     * Nombre o ruta de la imagen del documento de la licencia.
     */
    @Column(name = "imagen", nullable = false)
    private String imagen;

    /**
     * Almacén al que pertenece la licencia.
     * Relación uno a uno (1:1). Es el lado propietario: la clave ajena
     * almacen_id está en esta tabla y es única, así un almacén solo tiene
     * una licencia. No puede ser nulo.
     */
    @NotNull(message = "{msg.licencia.almacen.notNull}")
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "almacen_id", nullable = false, unique = true)
    private Almacen almacen;

    /**
     * Constructor sin `id` (lo genera la base de datos).
     * Se usa para crear una licencia de almacén nueva antes de insertarla.
     *
     * @param diaMatriculacion Día de matriculación de la licencia.
     * @param imagen           Imagen del documento de la licencia.
     * @param almacen          Almacén al que pertenece.
     */
    public LicenciaAlmacen(LocalDate diaMatriculacion, String imagen, Almacen almacen) {
        this.diaMatriculacion = diaMatriculacion;
        this.imagen = imagen;
        this.almacen = almacen;
    }
}
