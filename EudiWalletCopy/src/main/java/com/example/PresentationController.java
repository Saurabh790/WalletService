package com.example;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import java.util.Base64;
import org.springframework.web.bind.annotation.GetMapping;
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

 // Step 1: Generate QR Code with HTTPS URL
    @GetMapping("/generatePresentationQR")
    public ResponseEntity<Map<String, String>> generatePresentationQR() throws Exception {
        System.out.println("testing starts ");
        
        // Get the signed JWT token
        String jwt = tokenService.getSignedJWT();
        
        // Construct the openid4vp URL with client_id and request_uri
        String clientId = "https://walletservice-production.up.railway.app/"; // The client ID you mentioned
        String uri = "openid4vp://?client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8)
                   + "&request_uri=" + URLEncoder.encode(jwt, StandardCharsets.UTF_8);
        
        // Generate QR code image (Base64 encoded)
        byte[] qrCodeBytes = walletService.generateQRCode(uri, 500, 500);
        String qrCodeBase64 = Base64.getEncoder().encodeToString(qrCodeBytes);
        
        // Create response with both the URI and QR code Base64 image
        Map<String, String> response = new HashMap<>();
        response.put("authRequestUrl", uri);
        response.put("qrCodeBase64", qrCodeBase64);
        System.out.println("testing ends ");
        
        return ResponseEntity.ok(response);
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
