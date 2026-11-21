package org.alixar.daw2.JoseManuel.dwese_ticket_logger_webapp.repositories;

import org.alixar.daw2.JoseManuel.dwese_ticket_logger_webapp.entities.EtiquetaProducto;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio para gestionar las operaciones de acceso a datos
 * de la entidad EtiquetaProducto.
 * Además de las operaciones CRUD, permite comprobar duplicados.
 */
public interface EtiquetaProductoRepository extends JpaRepository<EtiquetaProducto, Long> {

    // Al CREAR: ¿existe ya una etiqueta con este código de barras?
    boolean existsByCodigoBarras(String codigoBarras);

    // Al EDITAR: ¿existe OTRA etiqueta (distinta de la actual) con este código?
    boolean existsByCodigoBarrasAndIdNot(String codigoBarras, Long id);

    // Al CREAR: ¿esta caja ya tiene una etiqueta asignada?
    boolean existsByCajaId(Long cajaId);

    // Al EDITAR: ¿esta caja ya tiene OTRA etiqueta distinta de la actual?
    boolean existsByCajaIdAndIdNot(Long cajaId, Long id);
}
