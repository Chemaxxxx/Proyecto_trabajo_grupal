package org.alixar.daw2.JoseManuel.dwese_ticket_logger_webapp.repositories;

import org.alixar.daw2.JoseManuel.dwese_ticket_logger_webapp.entities.Almacen;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio para gestionar las operaciones de acceso a datos
 * de la entidad Almacen.
 * Permite realizar operaciones CRUD sobre los almacenes.
 */
public interface AlmacenRepository extends JpaRepository<Almacen, Long> {
}
