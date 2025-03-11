package org.services;

import com.nimbusds.jose.util.Pair;
import com.paypal.api.payments.*;
import com.paypal.api.payments.Currency;
import com.paypal.api.payments.Phone;
import com.paypal.base.rest.PayPalRESTException;
import com.paypal.orders.Item;
import com.paypal.base.rest.APIContext;
import com.paypal.core.PayPalHttpClient;
import com.paypal.orders.*;
import com.paypal.orders.Order;
import org.entity.CustomUserDetails;
import org.entity.PaymentReference;
import org.repositories.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.utilities.CustomItems;
import org.utilities.RsaService;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    @Autowired
    private APIContext apiContext;

    SimpleDateFormat format = new SimpleDateFormat("yyyy-mm-dd");

    @Autowired
    private PaymentRepository paymentRepository;


    @Autowired
    private PayPalHttpClient client;


    @Autowired
    private RsaService rsaService;

    @Value(value = "${paypal.merchant-info}")
    private String merchant_email;



    public Item getItem(String name, String sku, Integer qty, String currencyCode, String unit_price) {
        Item item = new Item();
        item.quantity(String.valueOf(qty));
        item.unitAmount(new Money().currencyCode(currencyCode).value(unit_price));
        item.sku(sku);
        item.name(name);
        return item;
    }


    public Order getOrder(List<com.paypal.orders.Item>items, String currency_code_tax, Float tax_amt,
                          String return_url, String cancel_url) throws IOException {
        PurchaseUnitRequest unit = new PurchaseUnitRequest();
        unit.items(items);
        AmountWithBreakdown breakdown = new AmountWithBreakdown();
        List<Float>totalbaseAmount = items.stream().map(item->{
            return Float.parseFloat(item.quantity())*Float.parseFloat(item.unitAmount().value());
        }).toList();
        Float totalAmount = totalbaseAmount.stream().reduce(0.00f, Float::sum);
        breakdown.amountBreakdown(new AmountBreakdown()
                .itemTotal(new Money().currencyCode(currency_code_tax).value(totalAmount.toString()))
        );
        breakdown.amountBreakdown().taxTotal(
          new Money().currencyCode(currency_code_tax).value(tax_amt.toString())
        );
        totalAmount+=tax_amt;
        System.out.println(String.format("%.2f",totalAmount));
        breakdown.currencyCode(currency_code_tax).value(String.format("%.2f",totalAmount));

        unit.amountWithBreakdown(breakdown);
        OrderRequest request = new OrderRequest();
        request.checkoutPaymentIntent("AUTHORIZE");
        request.purchaseUnits(List.of(unit));
        request.applicationContext(new ApplicationContext().returnUrl(return_url).cancelUrl(cancel_url));
        OrdersCreateRequest creation = new OrdersCreateRequest().requestBody(request);
        return client.execute(creation).result();
    }


    public Order getOrdersAuthorizeRequest(String id) throws IOException {
        OrdersAuthorizeRequest authorizeRequest = new OrdersAuthorizeRequest(id);
        com.paypal.http.HttpResponse<Order>response = client.execute(authorizeRequest);
        return response.result();
    }

    public Order getCapture(String authId) throws IOException {
        OrdersCaptureRequest captureRequest = new OrdersCaptureRequest(authId);
        //HttpResponse<Order> response = (HttpResponse<Order>) client.execute(captureRequest).result();
        return client.execute(captureRequest).result();
    }

    public Order getOrderRequest(String orderId) throws IOException {
        OrdersGetRequest getRequest = new OrdersGetRequest(orderId);
        return client.execute(getRequest).result();
    }

    public Invoice getInvoice(List<InvoiceItem> items, BillingInfo billingInfo) throws PayPalRESTException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd z");
        dateFormat.setTimeZone(TimeZone.getDefault());
        PaymentReference reference = new PaymentReference();
        paymentRepository.save(reference);
        Long id = reference.getId();
        Invoice voice = new Invoice();
        voice.setId("INV-"+String.valueOf(id));
        voice.setInvoiceDate(dateFormat.format(new Date(System.currentTimeMillis())));
        PaymentDetail det = new PaymentDetail();
        voice.setMerchantInfo(new MerchantInfo().setEmail(merchant_email)
                .setFirstName("Shreeya").setLastName("Sen")
        );
        voice.setAllowPartialPayment(true);
        voice.setItems(items);
        voice.setBillingInfo(List.of(billingInfo));
        voice.setPaymentTerm(new PaymentTerm().setTermType("NET_10"));
        return voice.create(apiContext);
    }

    public List<BillingInfo> getBillingInfo(List<String>allEmails) {
        return allEmails.stream().map(BillingInfo::new).toList();
    }

    public void sendInvoice(String id) throws PayPalRESTException {
        Invoice invoice = Invoice.get(apiContext,id);
        System.out.println(invoice.getStatus());
        invoice.send(apiContext);
    }

    public Invoice getInv(String id) throws PayPalRESTException {
        return Invoice.get(apiContext,id);
    }

    public Image getQr(Invoice invoice) throws PayPalRESTException {
        Map<String,String>options =new HashMap<>();
        options.put("width","500");
        options.put("height","500");
        Image img = Invoice.qrCode(apiContext,invoice.getId(),options);
        return img;
    }

    public void record_payment(Invoice invoice) throws PayPalRESTException {
        invoice.recordPayment(apiContext,new PaymentDetail().setAmount(new Currency("SGD","5.99")).setMethod("CREDIT_CARD"));
    }




    public List<InvoiceItem> getInvoiceItems(List<CustomItems>items) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd");
        return items.stream().map(item->{
            return new InvoiceItem(item.name(),Float.parseFloat(item.qty()),
                    new Currency(item.unit_currency(),item.value()));
        }).toList();
    }

    public void savePaymentReference(String token, String payerId) {
        PaymentReference reference=new PaymentReference();
        String data = rsaService.jwtEncrypt(token,payerId);
        //reference.setData(data);
        paymentRepository.save(reference);
    }







}
