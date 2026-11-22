package org.alixar.daw2.JoseManuel.dwese_ticket_logger_webapp.repositories;

import org.alixar.daw2.JoseManuel.dwese_ticket_logger_webapp.entities.LicenciaAlmacen;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio para gestionar las operaciones de acceso a datos
 * de la entidad LicenciaAlmacen.
 * Además de las operaciones CRUD, permite comprobar duplicados.
 */
public interface LicenciaAlmacenRepository extends JpaRepository<LicenciaAlmacen, Long> {

    // Al CREAR: ¿este almacén ya tiene una licencia asignada?
    boolean existsByAlmacenId(Long almacenId);

    // Al EDITAR: ¿este almacén ya tiene OTRA licencia distinta de la actual?
    boolean existsByAlmacenIdAndIdNot(Long almacenId, Long id);
}
