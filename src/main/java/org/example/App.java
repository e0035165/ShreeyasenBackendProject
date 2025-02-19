package org.example;

import org.entity.CustomUserDetails;
import org.entity.Role;
import org.services.CustomUserDetailsService;
import org.services.RoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.List;

/**
 * Hello world!
 *
 */

@SpringBootApplication
@EntityScan(basePackages = {"org.entity"})
@ComponentScan(basePackages = {"org.repositories","org.entity","org.services","org.controllerz","org.utilities","org.example","org.filter"})
@EnableJpaRepositories(basePackages = {"org.repositories"})
public class App implements CommandLineRunner
{
    private static Logger LOG = LoggerFactory
            .getLogger(App.class);


    public static void main( String[] args )
    {

        SpringApplication.run(App.class,args);
    }

    @Autowired
    private RoleService roleService;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Override
    public void run(String... args) throws Exception {
        LOG.info("Command Line interface execution");
        List<org.entity.Role>allRoles = List.of(
                new org.entity.Role("ROLE_ADMIN"),
                new org.entity.Role("ROLE_MANAGER")
        );
        roleService.addRoles(allRoles);
        CustomUserDetails admin = (CustomUserDetails) userDetailsService.loadUserByUsername("Admin");
        admin.setRoles(List.of(roleService.getRole("ROLE_ADMIN"),
                                roleService.getRole("ROLE_MANAGER"),
                                new Role("ROLE_TESTER")
                             ));
        userDetailsService.addUser(admin);
        System.out.println("Completed admin setup");
    }
}
