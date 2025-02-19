package org.utilities;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jose.crypto.RSAEncrypter;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.text.ParseException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


@Component
public class RsaService {

    private static Logger log = Logger.getLogger(RsaService.class.getName());

    private RSAPublicKey rsaPublicKey;

    private RSAPrivateKey rsaPrivateKey;

    public RsaService(@Value(value="${jwt.public-key}") String public_filepath,
                      @Value(value = "${jwt.private-key}") String private_filepath) {
        File pubkey = new File(public_filepath);
        File pvtkey = new File(private_filepath);
        init(pubkey,pvtkey);
    }

    private void init(File pubkey, File pvtkey) {
        final BufferedReader pubkeyReader;
        try {
            pubkeyReader = new BufferedReader(new FileReader(pubkey));
            StringBuffer pubkeybuffer = new StringBuffer();
            pubkeyReader.lines().forEach(pubkeybuffer::append);
            KeyFactory factory = KeyFactory.getInstance("RSA");
            String pubkeyString  = pubkeybuffer.toString()
                    .replaceAll("-----BEGIN PUBLIC KEY-----","")
                    .replaceAll("-----END PUBLIC KEY-----","")
                    .replaceAll("\\r\\n", "")
                    .replaceAll("[\\r\\n]", "")
                    .replaceAll(System.lineSeparator(),"");
            byte[] pub_decoded = Base64.getDecoder().decode(pubkeyString);
            X509EncodedKeySpec pub_key_spec = new X509EncodedKeySpec(pub_decoded);

            this.rsaPublicKey=(RSAPublicKey) factory.generatePublic(pub_key_spec);
            final BufferedReader pvtkeyReader = new BufferedReader(new FileReader(pvtkey));
            StringBuffer pvtkeybuffer = new StringBuffer();
            pvtkeyReader.lines().forEach(pvtkeybuffer::append);
            String pvtkeyString  = pvtkeybuffer.toString()
                    .replaceAll("-----BEGIN PRIVATE KEY-----","")
                    .replaceAll("-----END PRIVATE KEY-----","")
                    .replaceAll("\\r\\n", "")
                    .replaceAll("[\\r\\n]", "")
                    .replaceAll(System.lineSeparator(),"");
            byte[] pvt_decoded = Base64.getDecoder().decode(pvtkeyString);
            PKCS8EncodedKeySpec pvt_key_spec = new PKCS8EncodedKeySpec(pvt_decoded);
            this.rsaPrivateKey=(RSAPrivateKey) factory.generatePrivate(pvt_key_spec);
            System.out.println("JWT Initialization success");
        } catch (FileNotFoundException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    public String jwtEncrypt(Map<String, Object> values) {
        try{
            JWSSigner signer = new RSASSASigner(this.rsaPrivateKey);
            JWTClaimsSet sets = new JWTClaimsSet.Builder()
                    .claim("username",values.get("username"))
                    .claim("password",values.get("password"))
                    .claim("email",values.get("email"))
                    .issueTime(new Date(System.currentTimeMillis()))
                    .expirationTime(new Date(System.currentTimeMillis()+5*1000*60)).build();
            SignedJWT object = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).build(), sets);
            object.sign(signer);
            JWSVerifier verifier = new RSASSAVerifier(this.rsaPublicKey);
            if(object.verify(verifier)) {
                JWEObject jweObject = new JWEObject(
                        new JWEHeader.Builder(JWEAlgorithm.RSA_OAEP_256,EncryptionMethod.A256GCM).contentType("JWT").build(),
                        new Payload(object)
                );
                RSAEncrypter encrypter = new RSAEncrypter(this.rsaPublicKey);
                jweObject.encrypt(encrypter);
                return jweObject.serialize();
            } else {
                throw new RuntimeException("JWS Verification has failed");
            }
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }

    }

    public Map<String,Object> jwtDecrypt(String jwt) {

        try {
            JWEObject object = JWEObject.parse(jwt);
            RSADecrypter decrypter = new RSADecrypter(this.rsaPrivateKey);
            object.decrypt(decrypter);

            SignedJWT Jwt = object.getPayload().toSignedJWT();
            JWSSigner signer = new RSASSASigner(this.rsaPrivateKey);
            JWSVerifier verifier = new RSASSAVerifier(this.rsaPublicKey);
            if(Jwt.verify(verifier)) {
                return Jwt.getPayload().toJSONObject();
            } else {
                throw new RuntimeException("JWS Verification has failed");
            }
        } catch (ParseException e) {
            throw new RuntimeException(e);
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }

    }

//    public static void main(String... args) throws JOSEException, ParseException {
//        RsaService service = new RsaService("src/main/resources/public_key.pem",
//                "src/main/resources/private_key.pem");
//        Map<String,Object>values = new HashMap<>();
//        values.put("username","username");
//        values.put("password","password");
//        values.put("email","email");
//        String jwt = service.jwtEncrypt(values);
//        System.out.println(jwt);
//        System.out.println(service.jwtDecrypt(jwt).get("email"));
//    }


}
