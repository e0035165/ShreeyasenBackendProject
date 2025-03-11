package org.controllerz;


import com.paypal.orders.Order;
import lombok.Getter;
import org.entity.PaymentReference;
import org.services.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;


@RestController
@RequestMapping(path = "/v1/payment")
@CrossOrigin(origins = {"http://localhost:5200","http://localhost:5400"}, allowedHeaders = {"Authorization", "Content-Type"})
public class PaymentController {

    @Autowired
    private PaymentService service;

    @GetMapping(path = "/success")
    public ResponseEntity<?> getApproval(@RequestParam(name = "token", required = true) String token,
                                         @RequestParam(name = "PayerID", required = true) String payerId) throws IOException {
        Order order = service.getOrdersAuthorizeRequest(token);
        service.savePaymentReference(token, payerId);
        return ResponseEntity.status(201).body(order.status());
    }

    @GetMapping(path="/cancel")
    public ResponseEntity<?> getCancel() {
        return ResponseEntity.badRequest().body("Payment has cancelled due to issues");
    }


}
