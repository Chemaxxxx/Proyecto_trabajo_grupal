package org.alixar.daw2.JoseManuel.dwese_ticket_logger_webapp.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Una etiqueta de producto identifica a una única caja mediante un código
 * de barras. Representa la etiqueta pegada a una caja del almacén.
 */
@Entity
@Table(name = "etiquetas_producto")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EtiquetaProducto {

    /**
     * Identificador único de la etiqueta.
     * Se genera automáticamente.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Código de barras de la etiqueta.
     * Es obligatorio, único y debe tener entre 8 y 14 caracteres.
     */
    @NotBlank(message = "{msg.etiqueta.codigoBarras.notBlank}")
    @Size(min = 8, max = 14, message = "{msg.etiqueta.codigoBarras.size}")
    @Column(name = "codigo_barras", nullable = false, unique = true, length = 14)
    private String codigoBarras;

    /**
     * Caja a la que pertenece la etiqueta.
     * Relación uno a uno (1:1). Es el lado propietario: la clave ajena
     * caja_id está en esta tabla y es única, así una caja solo tiene
     * una etiqueta. No puede ser nulo.
     */
    @NotNull(message = "{msg.etiqueta.caja.notNull}")
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "caja_id", nullable = false, unique = true)
    private Caja caja;

    /**
     * Constructor sin `id` (lo genera la base de datos).
     * Se usa para crear una etiqueta nueva antes de insertarla.
     *
     * @param codigoBarras Código de barras de la etiqueta.
     * @param caja         Caja a la que pertenece.
     */
    public EtiquetaProducto(String codigoBarras, Caja caja) {
        this.codigoBarras = codigoBarras;
        this.caja = caja;
    }
}
