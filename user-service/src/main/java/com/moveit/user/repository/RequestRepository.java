package com.moveit.user.repository;

import com.moveit.user.dto.RequestStatus;
import com.moveit.user.entity.RequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RequestRepository extends JpaRepository<RequestEntity, Integer> {

    boolean existsByUserUserIdAndRequestStatus(Integer userId, RequestStatus requestStatus);
}
