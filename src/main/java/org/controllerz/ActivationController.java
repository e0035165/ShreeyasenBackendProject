package org.controllerz;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.paypal.api.payments.BillingInfo;
import com.paypal.api.payments.Currency;
import com.paypal.api.payments.Invoice;
import com.paypal.api.payments.InvoiceItem;
import com.paypal.base.rest.PayPalRESTException;
import org.entity.CustomUserDetails;
import org.entity.Role;
import org.services.CustomUserDetailsService;
import org.services.PaymentService;
import org.services.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.utilities.CustomItems;
import org.utilities.RsaService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@RestController
@RequestMapping(path = "/v1/activation")
@CrossOrigin(origins = {"http://localhost:5200","http://localhost:5400"}, allowedHeaders = {"Authorization", "Content-Type"})
public class ActivationController {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private RsaService rsaService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleService roleService;

    @Autowired
    private PaymentService paymentService;

    private ObjectMapper objectMapper= new ObjectMapper();

    @Autowired
    private JavaMailSender javaMailSender;



    @PostMapping(path = "/")
    public ResponseEntity<String> activation(@RequestBody Map<String,Object>respBody) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails details = (CustomUserDetails) authentication.getPrincipal();
        details.setActivated(true);
        customUserDetailsService.addUser(details);
        if(respBody.get("username").equals(details.getUsername())) {
            return ResponseEntity.ok("Account for user "+details.getUsername()+" has been activated");
        } else {
            return ResponseEntity.badRequest().body("Wrong token has been added to request");
        }
    }

    @PostMapping(path="/addAnotherAdmin")
    public ResponseEntity<?> activateAnotherAdmin(@RequestBody Map<String,Object>respBody) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails details = (CustomUserDetails) authentication.getPrincipal();
        Optional<String> authority = details.getAuthorities().stream().map(GrantedAuthority::getAuthority)
                .toList().stream().filter(role->role.equalsIgnoreCase("ROLE_MANAGER"))
                .findFirst();
        CustomUserDetails newDetail = new CustomUserDetails();
        Role manager = roleService.getRole("ROLE_MANAGER");
        newDetail.setUsername((String) respBody.get("username"));
        newDetail.setPassword(passwordEncoder.encode((CharSequence) respBody.get("password")));
        newDetail.setEmail((String) respBody.get("email"));
        newDetail.setActivated(true);
        newDetail.setRoles(List.of(manager));
        String jwt = rsaService.jwtEncrypt(respBody);
        customUserDetailsService.addUser(newDetail);
        if(authority.isPresent()) {
            respBody.put("jwt","Bearer "+jwt);
            return ResponseEntity.status(HttpStatus.CREATED).body(respBody);
        } else {
            respBody.put("error", "Administrator is not authorized.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(respBody);
        }
    }

    @DeleteMapping(path = "/delete")
    public ResponseEntity<String> delete(@RequestParam(required = true, name = "user") String user) {
        CustomUserDetails User = (CustomUserDetails) customUserDetailsService.loadUserByUsername(user);
        customUserDetailsService.removeUser(User.getId());
        return ResponseEntity.status(204).body("User "+user+" deleted");
    }

    @PostMapping(path = "/addItems")
    public ResponseEntity<String> getAllItems(@RequestBody Map<String,Object>mapper) throws JsonProcessingException, PayPalRESTException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        System.out.println(mapper.get("items").toString());
        JsonNode jsonNode = objectMapper.valueToTree(mapper);
        ArrayNode subNodes = (ArrayNode) jsonNode.get("items");
        ArrayNode subEmails = (ArrayNode) jsonNode.get("emails");
        List<InvoiceItem>customItems = getCustomItems(subNodes);
        List<BillingInfo>emails = getAllEmails(subEmails);
        Invoice invoice = paymentService.getInvoice(customItems,emails.get(0));
        paymentService.sendInvoice(invoice.getId());
        invoice = paymentService.getInv(invoice.getId());
        System.out.println("Final status: "+invoice.getStatus());
        System.out.println(invoice.toJSON());
        return ResponseEntity.ok("Invoice sent");
    }

    private List<BillingInfo>getAllEmails(ArrayNode emails) {
        ArrayList<BillingInfo>allWorks = new ArrayList<>();
        for(int i=0;i< emails.size();++i) {
            allWorks.add(new BillingInfo(emails.get(i).asText()));
        }
        return allWorks;
    }

    private List<InvoiceItem>getCustomItems(ArrayNode nodes) {
        ArrayList<InvoiceItem>allWorks = new ArrayList<>();
        for(int i=0;i< nodes.size();++i) {
            allWorks.add(
                    new InvoiceItem().setName(nodes.get(i).get("name").asText())
                            .setQuantity((float) nodes.get(i).get("qty").asDouble())
                            .setUnitPrice(new Currency(nodes.get(i).get("unit_currency").asText(),nodes.get(i).get("value").asText()))
            );
        }
        return allWorks;
    }



}
