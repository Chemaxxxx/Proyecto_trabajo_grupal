package org.alixar.daw2.JoseManuel.dwese_ticket_logger_webapp.repositories;

import org.alixar.daw2.JoseManuel.dwese_ticket_logger_webapp.entities.Pasillo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repositorio para gestionar las operaciones de acceso a datos
 * de la entidad Pasillo.
 * Permite realizar operaciones CRUD y consultas personalizadas.
 */
public interface PasilloRepository extends JpaRepository<Pasillo, Long> {

    /**
     * Comprueba si existe un pasillo con el número indicado.
     *
     * @param numero número del pasillo que se quiere comprobar
     * @return true si existe un pasillo con ese número, false en caso contrario
     */
    @Query("SELECT COUNT(p) > 0 FROM Pasillo p WHERE p.numero = :numero")
    boolean existsPasilloByNumero(@Param("numero") Integer numero);

    /**
     * Comprueba si existe otro pasillo con el número indicado,
     * excluyendo el pasillo cuyo identificador se proporciona.
     *
     * @param numero número del pasillo que se quiere comprobar
     * @param id identificador del pasillo que se quiere excluir
     * @return true si existe otro pasillo con ese número, false en caso contrario
     */
    @Query("SELECT COUNT(p) > 0 FROM Pasillo p WHERE p.numero = :numero AND p.id != :id")
    boolean existsPasilloByNumeroAndNotId(@Param("numero") Integer numero, @Param("id") Long id);
}