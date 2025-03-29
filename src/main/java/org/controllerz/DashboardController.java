package org.controllerz;


import org.entity.Contacts;
import org.entity.Documents;
import org.services.ContactService;
import org.services.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping(path = "/v1/Dashboard")
@CrossOrigin(origins = {"http://localhost:3080","http://localhost:5400"}, allowedHeaders = {"Authorization", "Content-Type"})
public class DashboardController {
    @Autowired
    private ContactService contactService;



    @GetMapping(path="/{username}/ContactDetails")
    public ResponseEntity<List<Contacts>> getAlldetails() {
        List<Contacts>allContacts = contactService.getAllContacts();
        return ResponseEntity.ok().body(allContacts);
    }





}
