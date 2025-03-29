package org.controllerz;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.paypal.api.payments.BillingInfo;
import com.paypal.api.payments.Currency;
import com.paypal.api.payments.Invoice;
import com.paypal.api.payments.InvoiceItem;
import com.paypal.base.rest.PayPalRESTException;
import com.paypal.orders.Item;
import com.paypal.orders.Order;
import org.entity.CustomUserDetails;
import org.entity.Documents;
import org.entity.Role;
import org.services.CustomUserDetailsService;
import org.services.DocumentService;
import org.services.PaymentService;
import org.services.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.utilities.CustomItems;
import org.utilities.KeyTracker;
import org.utilities.RsaService;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipOutputStream;


@RestController
@RequestMapping(path = "/v1/activation")
@CrossOrigin(origins = {"http://localhost:3080","http://localhost:5400"}, allowedHeaders = {"Authorization", "Content-Type"})
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

    @Autowired
    private DocumentService documentService;

    private ObjectMapper objectMapper= new ObjectMapper();

    @Autowired
    private JavaMailSender javaMailSender;



    @PostMapping(path = "/")
    public ResponseEntity<?> activation(@RequestBody Map<String,Object>respBody, @RequestParam(required = true, value = "role") String correct_role) {
        //Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails details = (CustomUserDetails) customUserDetailsService.loadUserByUsername((String) respBody.get("username"));
        details.setActivated(true);
        customUserDetailsService.addUser(details);
        String authority = details.getAuthorities().stream().toList().get(0).getAuthority();
        System.out.println(authority);
        respBody.put("authority",authority);
        System.out.println(respBody.get("username").toString());
        System.out.println(details.getUsername());
        if(respBody.get("username").equals(details.getUsername()) && authority.equals(correct_role)) {
            return ResponseEntity.ok(respBody);
        } else {
            return ResponseEntity.badRequest().body("Wrong token has been added to request");
        }
    }


    @PostMapping("/admin/resume/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file, @RequestParam("title") String title) {
        try {
            Documents savedDoc = documentService.saveFile(file,title);
            String valueProcessedString = KeyTracker.cleanResume(KeyTracker.numberProcessing(documentService.readResume(savedDoc.getId())));
            System.out.println(valueProcessedString);
            return ResponseEntity.ok("File uploaded successfully with ID: " + savedDoc.getId());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("File upload failed.");
        }
    }

    @GetMapping("/admin/getAllResumes")
    public ResponseEntity<?> getAllResumes() {
        List<Documents>all = documentService.getAllFiles();
        return ResponseEntity.ok(all);
    }

    @DeleteMapping("/admin/clearAllResumes")
    public ResponseEntity<?> clearAllResumes() {
        documentService.clearFiles();
        return ResponseEntity.status(HttpStatus.UNAVAILABLE_FOR_LEGAL_REASONS).body("body");
    }





    @GetMapping(path = "/admin/getResumes")
    public ResponseEntity<byte[]> getAllFiles(@RequestParam(value = "items") String item) throws IOException {
        List<String>values = Arrays.stream(item.split(",")).toList();
        System.out.println(values);

        System.out.println(item);
        List<Documents>allDocuments = documentService.getRelevantDocuments(values);
        File cFile = new File("src/main/resources/compresssed.zip");
        InputStreamResource resource = new InputStreamResource(new FileInputStream(cFile));

        // Creating a new instance of HttpHeaders Object
        HttpHeaders headers = new HttpHeaders();

        // Setting up values for contentType and headerValue
        String contentType = "application/zip";
        String headerValue = "attachment; filename=\"" + resource.getFilename() + "\"";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, headerValue)
                .body(resource.getContentAsByteArray());

    }


    @GetMapping(path = "/user/getResumes")
    public ResponseEntity<byte[]> getAllFilesUsers(@RequestParam(value = "items") String item) throws IOException {
        List<String>values = Arrays.stream(item.split(",")).toList();
        System.out.println(values);

        System.out.println(item);
        List<Documents>allDocuments = documentService.getRelevantDocuments(values);
        File cFile = new File("src/main/resources/compresssed.zip");
        InputStreamResource resource = new InputStreamResource(new FileInputStream(cFile));

        // Creating a new instance of HttpHeaders Object
        HttpHeaders headers = new HttpHeaders();

        // Setting up values for contentType and headerValue
        String contentType = "application/zip";
        String headerValue = "attachment; filename=\"" + resource.getFilename() + "\"";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, headerValue)
                .body(resource.getContentAsByteArray());

    }

    @PostMapping("/sendOrder")
    public ResponseEntity<?> sendOrders(@RequestBody Map<String,Object>body) throws IOException {
        JsonNode node = objectMapper.valueToTree(body);
        ArrayNode items = (ArrayNode) node.get("items");
        List<Item>allItems = new ArrayList<>();
        ArrayNode orderStatusLinks = objectMapper.createArrayNode();
        //String name, String sku, Integer qty, String currencyCode, String unit_price
        for(int i=0;i< items.size();++i) {
            JsonNode jtem = items.get(i);
            System.out.println(jtem.toPrettyString());
            Item customItem = paymentService.getItem(jtem.get("name").asText(), jtem.get("sku").asText(),
                    jtem.get("qty").asInt(),jtem.get("currencyCode").asText(), jtem.get("unit_price").asText());
            allItems.add(customItem);
        }
        Order order = paymentService.getOrder(allItems,"SGD",2.66f,"http://localhost:3080/payment/success","http://localhost:3080/payment/failure");
        order.links().stream().forEach(link->{
            orderStatusLinks.add(objectMapper.createObjectNode()
                    .put("rel",link.rel())
                    .put("href",link.href())
                    .put("method",link.method())
            );
            System.out.println(link.href()+" rel: "+link.rel()+" method: "+link.method());
        });
        return ResponseEntity.ok(orderStatusLinks);
    }

    @GetMapping("/admin/resume/{id}")
    public ResponseEntity<byte[]> getFile(@PathVariable Integer id) {
        Documents document = documentService.getFile(id);
        if (document == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + document.getTitle() + "\"")
                .header(HttpHeaders.CONTENT_TYPE, document.getFileType())
                .body(document.getData());
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
