package com.moveit.user.repository;

import com.moveit.user.entity.TicketEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<TicketEntity,Integer> {

    Optional<TicketEntity> findByValidationToken(String validationToken);

    boolean existsByValidationToken(String validationToken);
}
