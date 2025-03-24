package it.aulab.progetto_finale_docente.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import it.aulab.progetto_finale_docente.models.CareerRequest;

public interface CareerRequestRepository extends CrudRepository<CareerRequest, Long> {
    List<CareerRequest> findByIsCheckedFalse();

    @Query(value = "SELECT user_id FROM users_roles", nativeQuery = true)
    List<Long> findAllUserIds();

    @Query(value = "SELECT role_id FROM users_roles WHERE user_id = :id ", nativeQuery = true)
    List<Long> findByUserId(@Param("id") Long id); 
}
