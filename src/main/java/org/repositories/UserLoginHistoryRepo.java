package org.repositories;

import org.entity.UserLoginHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface UserLoginHistoryRepo extends JpaRepository<UserLoginHistory,Long>{

    
} 
