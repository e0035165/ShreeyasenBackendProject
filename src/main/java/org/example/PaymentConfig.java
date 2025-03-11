package org.example;


import com.paypal.base.rest.APIContext;
import com.paypal.core.PayPalEnvironment;
import com.paypal.core.PayPalHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PaymentConfig {

    @Value(value = "${paypal.client-id}")
    private String client_id;

    @Value(value = "${paypal.client-secret}")
    private String client_secret;

    @Value(value = "${paypal.mode}")
    private String sandbox;

    @Bean
    public APIContext getAPIContext() {
        return new APIContext(client_id,client_secret,sandbox);
    }

    @Bean
    public PayPalHttpClient getClient() {
        if(sandbox.equalsIgnoreCase("live")) {
            PayPalHttpClient client = new PayPalHttpClient(new PayPalEnvironment.Live(client_id,client_secret));
            return client;
        } else {
            PayPalHttpClient client = new PayPalHttpClient(new PayPalEnvironment.Sandbox(client_id,client_secret));
            return client;
        }


    }


}
