package org.services;


import org.entity.Role;
import org.repositories.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RoleService {
    @Autowired
    private RoleRepository repo;

    public Role addRole(Role role) {
        repo.save(role);
        return role;
    }

    public List<Role> addRoles(List<Role> roleList) {
        roleList.stream().forEach(this::addRole);
        return roleList;
    }

    public List<Role> getAllRoles() {
        return repo.findAll();
    }

    public Role getRole(String roleName) {
        Optional<Role> R = repo.findAll().stream().filter(role->role.getName().equals(roleName)).findFirst();
        return R.orElse(null);
    }

}
