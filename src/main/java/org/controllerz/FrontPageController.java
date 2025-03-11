package org.controllerz;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nimbusds.jose.JOSEException;
import org.entity.CustomUserDetails;
import org.entity.Role;
import org.services.CustomUserDetailsService;
import org.services.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.utilities.RsaService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(path = "/v1/frontPage")
@CrossOrigin(origins = {"http://localhost:5200","http://localhost:5400"}, allowedHeaders = {"Authorization", "Content-Type"})
public class FrontPageController {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private RoleService r_service;

    @Autowired
    private RsaService rsaService;

    @Autowired
    private PasswordEncoder encoder;

    private ObjectMapper objectMapper=new ObjectMapper();

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody Map<String,Object> loginDetails) {
        String username = loginDetails.get("username").toString();
        String password = loginDetails.get("password").toString();
        String email = loginDetails.get("email").toString();
        ObjectNode node = objectMapper.createObjectNode();
        node.put("jwt",rsaService.jwtEncrypt(loginDetails));
        CustomUserDetails details = (CustomUserDetails) userDetailsService.loadUserByUsername(username);
        if(details==null)
            return ResponseEntity.status(500).body("User does not exist");

        if(!details.isEnabled())
            return ResponseEntity.status(500).body("User is not enabled");

        if(encoder.matches(password, details.getPassword()))
            return ResponseEntity.status(200).body(node.asText());
        else
            return ResponseEntity.status(401).body("User entered the wrong password");

    }

    @PostMapping(path = "/signup")
    public ResponseEntity<String> signup(@RequestBody Map<String,Object>loginDetails) {
        Role role = r_service.getRole("ROLE_USER");
        if(role==null)
            role=r_service.addRole(new Role("ROLE_USER"));
        ObjectNode node = objectMapper.createObjectNode();
        CustomUserDetails details = new CustomUserDetails();
        details.setUsername((String) loginDetails.get("username"));
        details.setPassword(encoder.encode((String)loginDetails.get("password")));
        details.setEmail((String) loginDetails.get("email"));
        details.setRoles(List.of(role));
        node.put("jwt", "Bearer "+rsaService.jwtEncrypt(loginDetails));
        if(userDetailsService.loadUserByUsername(details.getUsername())==null) {
            userDetailsService.addUser(details);
            node.put("comments","User "+details.getUsername()+" Has been activated. Pending activation.");
            return ResponseEntity.status(HttpStatus.CREATED).body(node.toPrettyString());
        } else {
            return ResponseEntity.badRequest().body("User already exists");
        }


    }
}
