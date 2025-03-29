package org.example;

import com.paypal.orders.Item;
import com.paypal.orders.Order;
import com.paypal.orders.OrdersAuthorizeRequest;
import org.entity.CustomUserDetails;
import org.entity.Role;
import org.services.CustomUserDetailsService;
import org.services.PaymentService;
import org.services.RoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.temporal.TemporalUnit;
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

    @Autowired
    private PaymentService paymentService;

    @Override
    public void run(String... args) throws Exception {
        LOG.info("Command Line interface execution");
        List<org.entity.Role>allRoles = List.of(
                new org.entity.Role("ROLE_ADMIN"),
                new org.entity.Role("ROLE_MANAGER"),
                new org.entity.Role("ROLE_TESTER"),
                new org.entity.Role("ROLE_USER")
        );
        roleService.addRoles(allRoles);
        CustomUserDetails admin = (CustomUserDetails) userDetailsService.loadUserByUsername("Admin");
        admin.setRoles(List.of(roleService.getRole("ROLE_ADMIN"),
                                roleService.getRole("ROLE_MANAGER")
                             ));
        userDetailsService.addUser(admin);
        System.out.println("Completed admin setup");
        Item item = paymentService.getItem("Socks","socks",4,"SGD","4.99");
        Item itemTwo = paymentService.getItem("Shoes","shoes",1,"SGD","64.99");
        Order order = paymentService.getOrder(List.of(item,itemTwo),"SGD",12.99f,"http://localhost:3080/payment/success","http://localhost:3080/payment/failure");
        System.out.println(order.id());
        System.out.println(order.status());
        //System.out.println();
        order.links().stream().forEach(link->{
            System.out.println(link.href()+" rel: "+link.rel()+" method: "+link.method());
        });
        System.out.println("Payment Test Completed");
    }



    @Bean
    public RestTemplate getRestTemplate() {
        return new RestTemplateBuilder().connectTimeout(Duration.ofSeconds(2)).build();
    }
}
