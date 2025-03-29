package org.controllerz;


import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/v1")
@CrossOrigin(origins = {"http://localhost:3080","http://localhost:5400"}, allowedHeaders = {"Authorization", "Content-Type"})
public class GeneralController {

    @GetMapping(path = "/logout")
    public String logout() {
        return "logout completed";
    }

}
