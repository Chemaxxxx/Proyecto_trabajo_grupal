package org.alixar.daw2.JoseManuel.dwese_ticket_logger_webapp.repositories;

import org.alixar.daw2.JoseManuel.dwese_ticket_logger_webapp.entities.Caja;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio para gestionar las operaciones de acceso a datos
 * de la entidad Caja.
 * Permite realizar operaciones CRUD sobre las cajas.
 */
public interface CajaRepository extends JpaRepository<Caja, Long> {
}
