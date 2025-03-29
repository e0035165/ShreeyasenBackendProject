package org.controllerz;


import org.services.CustomUserDetailsService;
import org.services.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping(path = "/github/oauth2")
@CrossOrigin(origins = {"http://localhost:3080","http://localhost:5400"}, allowedHeaders = {"Authorization", "Content-Type"})
public class GithubLoginController {
    @Autowired
    private RestTemplate template;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private EmailService emailService;

    @Value(value ="${github.client_id}")
    private String client_id;


    @Value(value = "${github.client_secret}")
    private String client_secret;


    private String Emailmessage = "Dear {user} " +System.lineSeparator()+
            "Your generated github token is {token}. Please login within 3 mins of token validity."+System.lineSeparator()
            +"Thanks and Regards, {user}";




    @GetMapping("/callback")
    public ResponseEntity<Map> getAccessToken(@RequestParam(value = "code") String code) {
        String url = "https://github.com/login/oauth/access_token";
        HttpHeaders httpHeaders = new HttpHeaders();
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("client_id", client_id)
                .queryParam("client_secret", client_secret)
                .queryParam("code", code);
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        HttpEntity<String> entity = new HttpEntity<>(httpHeaders);
        ResponseEntity<Map> resp = template.exchange(uriBuilder.toUriString(), HttpMethod.POST, entity, Map.class);
        return resp;
    }

//    @GetMapping("/approve")
//    public ResponseEntity<Map>

    @GetMapping("/validate")
    public String getUserInfo(@RequestParam(value = "accessToken") String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        String userUrl = "https://api.github.com/user";
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = template.exchange(userUrl, HttpMethod.GET, entity, String.class);
        return response.getBody();
    }


    @PostMapping("/login/code")
    public ResponseEntity<Map> loginDeviceDetails(@RequestParam(required = true, value = "scope") String scope,
                                                  @RequestBody Map<String,Object>map) {
        String userUrl = "https://github.com/login/device/code";
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        HttpEntity<String> entity = new HttpEntity<>(headers);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        String cliet_id = map.get("client_id").toString();
        String client_sec = map.get("client_secret").toString();
        String email = map.get("email").toString();
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(userUrl)
                .queryParam("client_id", cliet_id)
                .queryParam("scope", scope);
        ResponseEntity<Map>getResult = template.exchange(uriBuilder.toUriString(),HttpMethod.POST,entity,Map.class);
        String mailMessage = "The token number is "+ Objects.requireNonNull(getResult.getBody()).get("user_code");
        emailService.sendSimpleEmail(email,"User request",mailMessage);
        return getResult;

    }

}
