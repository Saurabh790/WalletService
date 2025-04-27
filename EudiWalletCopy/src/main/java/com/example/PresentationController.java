package com.example;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import java.util.Base64;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.service.TokenService;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@RestController
public class PresentationController {

    @Autowired
    private TokenService tokenService;

    @Autowired
    private WalletService walletService;

//  Step 1: Generate QR Code with HTTPS URL
//    @GetMapping("/generatePresentationQR")
//    public ResponseEntity<Map<String, String>> generatePresentationQR() throws Exception {
//        System.out.println("testing starts ");
//        
//        // Get the signed JWT token
//        String jwt = tokenService.getSignedJWT();
//        
//        // Construct the openid4vp URL with client_id and request_uri
//        String clientId = "walletservice-production.up.railway.app/"; // The client ID you mentioned
//        String uri = "openid4vp://?client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8)
//                   + "&request_uri=" + URLEncoder.encode(jwt, StandardCharsets.UTF_8);
//        
//        // Generate QR code image (Base64 encoded)
//        byte[] qrCodeBytes = walletService.generateQRCode(uri, 500, 500);
//        String qrCodeBase64 = Base64.getEncoder().encodeToString(qrCodeBytes);
//        
//        // Create response with both the URI and QR code Base64 image
//        Map<String, String> response = new HashMap<>();
////        response.put("authRequestUrl", uri);
//        response.put("qrCodeBase64", qrCodeBase64);
//        System.out.println("testing ends ");
//        
//        return ResponseEntity.ok(response);
//    }
    
    
//  Step 1: Generate QR Code with HTTPS URL
    @GetMapping("/generatePresentationQR")
    public ResponseEntity<Map<String, String>> generatePresentationQR() throws Exception {
        System.out.println("Testing starts");

        // Get the signed JWT token
        String jwt = tokenService.getSignedJWT();
        
        // URL encode the JWT token for inclusion in the URI
        String encodedJwt = URLEncoder.encode(jwt, StandardCharsets.UTF_8);
        
        // The base URL where the JWT will be hosted (make sure this is a valid endpoint that can serve the JWT)
        String baseUrl = "https://walletservice-production.up.railway.app/jwt/";
        String requestUri = baseUrl + encodedJwt; // This URL should be accessible and return the JWT
        
        // Construct the openid4vp URL with client_id and request_uri
        String clientId = "walletservice-production.up.railway.app"; // The client ID
        String uri = "openid4vp://?client_id=" + clientId + "&request_uri=" + URLEncoder.encode(requestUri, StandardCharsets.UTF_8);
        
        // Generate the QR code based on the URI
        byte[] qrCodeBytes = walletService.generateQRCode(uri, 500, 500);
        String qrCodeBase64 = Base64.getEncoder().encodeToString(qrCodeBytes);
        
        // Create response with both the URI and QR code Base64 image
        Map<String, String> response = new HashMap<>();
        response.put("authRequestUrl", uri);  // URL for the authorization request
        response.put("qrCodeBase64", qrCodeBase64); // Base64 encoded QR code image
        
        System.out.println("Testing ends  uri  " + uri);
        System.out.println("Testing ends  qrCodeBase64  " + qrCodeBase64);
        
        return ResponseEntity.ok(response);
    }


    @GetMapping("/jwt/{jwtToken}")
    public ResponseEntity<String> getJwt(@PathVariable String jwtToken) {
        // Logic to retrieve the JWT using the token
        return ResponseEntity.ok(jwtToken);  // For example, you return the JWT directly
    }


    // Step 2: Serve JWT when EUDI Wallet hits your server
    @GetMapping("/request")
    public ResponseEntity<Map<String, String>> getRequest(@RequestParam("id") String id) throws Exception {
        String jwt = tokenService.getSignedJWT();

        Map<String, String> response = new HashMap<>();
        response.put("request", jwt); // OpenID4VP expects { "request": "jwt" }

        return ResponseEntity.ok(response);
    }
}
