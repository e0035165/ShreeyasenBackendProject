package org.controllerz;


import org.entity.CustomUserDetails;
import org.services.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@RequestMapping(path = "/v1/activation")
@CrossOrigin(origins = {"http://localhost:5200","http://localhost:5400"}, allowedHeaders = {"Authorization", "Content-Type"})
public class ActivationController {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @PostMapping(path = "/")
    public ResponseEntity<String> activation(@RequestBody Map<String,Object>respBody) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails details = (CustomUserDetails) authentication.getPrincipal();
        if(respBody.get("username").equals(details.getUsername())) {
            return ResponseEntity.ok("Account for user "+details.getUsername()+" has been activated");
        } else {
            return ResponseEntity.badRequest().body("Wrong token has been added to request");
        }
    }

    @DeleteMapping(path = "/delete")
    public ResponseEntity<String> delete(@RequestParam(required = true, name = "user") String user) {
        CustomUserDetails User = (CustomUserDetails) customUserDetailsService.loadUserByUsername(user);
        customUserDetailsService.removeUser(User.getId());
        return ResponseEntity.status(204).body("User "+user+" deleted");
    }

}
