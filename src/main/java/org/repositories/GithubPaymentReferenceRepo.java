package org.repositories;

import org.entity.PaymentReference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GithubPaymentReferenceRepo extends JpaRepository<PaymentReference,Long> {
}
