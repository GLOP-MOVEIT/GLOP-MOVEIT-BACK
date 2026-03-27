package com.moveit.user.repository;

import com.moveit.user.entity.TicketEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketRepository extends JpaRepository<TicketEntity,Integer> {

    Page<TicketEntity> findByUserUserId(Integer userId, Pageable pageable);
}