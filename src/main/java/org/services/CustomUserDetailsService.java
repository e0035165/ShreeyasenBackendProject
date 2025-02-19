package org.services;


import org.entity.CustomUserDetails;
import org.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository repo;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<CustomUserDetails> opt = repo.findAll().stream().filter(user->user.getUsername()
                .equals(username)).findFirst();
        return opt.orElse(null);
    }

    public CustomUserDetails addUser(CustomUserDetails details) {
        repo.save(details);
        return details;
    }

    public boolean removeUser(long id) {
        repo.deleteById(id);
        return true;
    }
}
