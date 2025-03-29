package org.repositories;

import org.entity.Documents;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface DocumentRepo extends JpaRepository<Documents,Integer> {
}
