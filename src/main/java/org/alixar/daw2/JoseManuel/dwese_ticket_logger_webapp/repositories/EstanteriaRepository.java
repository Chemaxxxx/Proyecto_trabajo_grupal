package org.alixar.daw2.JoseManuel.dwese_ticket_logger_webapp.repositories;

import org.alixar.daw2.JoseManuel.dwese_ticket_logger_webapp.entities.Estanteria;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio para gestionar las operaciones de acceso a datos
 * de la entidad Estanteria.
 * Permite realizar operaciones CRUD sobre las estanterías.
 */
public interface EstanteriaRepository extends JpaRepository<Estanteria, Long> {
}