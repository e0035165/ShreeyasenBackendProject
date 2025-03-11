package org.controllerz;


import org.entity.Contacts;
import org.services.ContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path = "/v1/Dashboard")
@CrossOrigin(origins = {"http://localhost:5200","http://localhost:5400"}, allowedHeaders = {"Authorization", "Content-Type"})
public class DashboardController {
    @Autowired
    private ContactService contactService;

    @GetMapping(path="/{username}/ContactDetails")
    public ResponseEntity<List<Contacts>> getAlldetails() {
        List<Contacts>allContacts = contactService.getAllContacts();
        return ResponseEntity.ok().body(allContacts);
    }


}
