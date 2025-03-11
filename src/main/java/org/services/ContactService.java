package org.services;

import org.entity.Contacts;
import org.entity.CustomUserDetails;
import org.repositories.ContactsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ContactService {
    @Autowired
    private ContactsRepository repository;

    public List<Contacts> getAllContacts() {
        return repository.findAll();
    }

    public Contacts getContact(CustomUserDetails details) {
        Optional<Contacts>cta = repository.findAll().stream().filter(contact->{
            return contact.getUserDetails().equals(details);
        }).findFirst();
        return cta.orElse(null);
    }

    public void addContacts(List<Contacts>contacts) {
        repository.saveAll(contacts);
    }

    public void addContact(Contacts ctc) {
        repository.save(ctc);
    }


}
