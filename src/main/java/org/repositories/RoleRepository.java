package org.repositories;


import org.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

@Repository
public interface RoleRepository extends JpaRepository<Role,Integer> {
}
