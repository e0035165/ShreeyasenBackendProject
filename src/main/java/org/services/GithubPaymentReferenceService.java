package org.services;

import org.entity.PaymentReference;
import org.repositories.GithubPaymentReferenceRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GithubPaymentReferenceService {
    @Autowired
    private GithubPaymentReferenceRepo referenceRepo;

    public void addReference(PaymentReference reference) {
        referenceRepo.save(reference);
    }

    public PaymentReference getPaymentReference(String client_id) {
        Optional<PaymentReference> opt = referenceRepo.findAll().stream()
                .filter(reference -> reference.getClient_id().equalsIgnoreCase(client_id))
                .findFirst();
        return opt.orElse(null);
    }

}
