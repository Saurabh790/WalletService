package com.example.controller;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.customexception.VpTokenValidationException;
import com.example.helper.PrivatePublicKeys;
import com.example.service.TokenService;
import com.example.service.qrService;
import com.nimbusds.jose.JWEDecrypter;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.ECDHDecrypter;
import com.nimbusds.jose.crypto.ECDSAVerifier;
import com.nimbusds.jwt.SignedJWT;

@Controller
public class WalletController {
	
	@Autowired
	qrService walletService;
	
	@Autowired
	TokenService tokenService;
	
	@Autowired
	PrivatePublicKeys privatePublicKeys;
	
	Logger log = LoggerFactory.getLogger(WalletController.class);
//	
//	
////	@GetMapping("/")
////	public ModelAndView getAuthorizationRequest() {
////		try {
////			
////			   log.info("inside getAuthorizationRequest");
////		
////		       String jwtString = tokenService.getSignedJWT();
////		       
////		       log.info("jwtString : {}" , jwtString);
////		       
////		       System.out.println(jwtString);
////		
////			   String clientId = "eudiwallet-service-alexvicks567-dev.apps.rm3.7wse.p1.openshiftapps.com";
////			   
//////			   String authRequestUrl = "openid4vp://?client_id=" + clientId +
//////		                "&request=" + URLEncoder.encode(jwtString, StandardCharsets.UTF_8);
////			   
////			   String uri = "openid4vp://?request="+ URLEncoder.encode(jwtString, StandardCharsets.UTF_8);
////			  
////			   byte[]  qrCodeBytes = walletService.generateQRCode(uri, 500, 500);
////			   String qrCodeBase64 = Base64.getEncoder().encodeToString(qrCodeBytes);
////			   
////		
////		        ModelAndView modelAndView = new ModelAndView("index");
////	//	        modelAndView.setViewName("index"); // Set the view name (HTML page)
////		        modelAndView.addObject("authRequestUrl", uri); // Add the authRequestUrl as a link
////		        modelAndView.addObject("qrCodeBase64", qrCodeBase64); // Add the QR code as a Base64 string
////		        
////		        return modelAndView;
////		}
////		catch (Exception e) {
////			System.out.println(e.getMessage());
////			return new ModelAndView();
////		}
////			   
////		
////	}
//	
////	@GetMapping("/generatePresentationQR")
////	public ResponseEntity<Map<String, String>> generatePresentationQR() throws Exception {
////	    // Step 1: Generate Signed JWT
////	    String jwtString = tokenService.getSignedJWT();
////
////	    // Step 2: Build the openid4vp URI
////	    String uri = "openid4vp://?request=" + URLEncoder.encode(jwtString, StandardCharsets.UTF_8);
////
////	    // Step 3: Generate QR Code (assuming you have walletService bean)
////	    byte[] qrCodeBytes = walletService.generateQRCode(uri, 500, 500);
////
////	    // Step 4: Encode QR Code to Base64
////	    String qrCodeBase64 = Base64.getEncoder().encodeToString(qrCodeBytes);
////
////	    // Step 5: Return both URI and QR Code Base64
////	    Map<String, String> response = Map.of(
////	            "authRequestUrl", uri,
////	            "qrCodeBase64", qrCodeBase64
////	    );
////
////	    return ResponseEntity.ok(response);
////	}
//
//	
//	
//	@GetMapping(value = "/generatePresentationQR", produces = "image/png")
//	public ResponseEntity<byte[]> generatePresentationQR() throws Exception {
//	    String jwtString = tokenService.getSignedJWT();
//	    String uri = "openid4vp://?request=" + URLEncoder.encode(jwtString, StandardCharsets.UTF_8);
//
//	    byte[] qrCodeBytes = walletService.generateQRCode(uri, 500, 500);
//
//	    return ResponseEntity
//	            .ok()
//	            .header("Content-Disposition", "inline; filename=\"qr.png\"")
//	            .body(qrCodeBytes);
//	}
//	
//	
//	
	@PostMapping("/walletResponse")
	@ResponseBody
	public ResponseEntity<String> verifyingToken(@RequestParam("response") String response) throws VpTokenValidationException {
		try {
		
		PrivateKey privateKey =privatePublicKeys.loadPrivateKey("private-key-pkcs8.pm");
			
		X509Certificate certificate =privatePublicKeys.loadX509Certificate("x509certificate.pem");
		
		
		JWEObject jweObject = JWEObject.parse(response); //Parsing JWE
	    
	    JWEDecrypter decrypter = new ECDHDecrypter((ECPrivateKey) privateKey); //Decrypt JWE
	    
	    jweObject.decrypt(decrypter);
	   
	    String vp_token = jweObject.getPayload().toString(); //Getting the payload
	    
	    System.out.println("vp_token"+vp_token);
	   
	    SignedJWT signedJWT = SignedJWT.parse(vp_token);
	    
	    JWSVerifier jwsVerifier = new ECDSAVerifier((ECPublicKey) certificate.getPublicKey());
	    
	    if(!signedJWT.verify(jwsVerifier)) { //verifiying the signature
	    	//return new ResponseEntity<String>("Invalid VP token", HttpStatus.BAD_REQUEST);
	    	System.out.println("invalid VP token");
	    	throw new VpTokenValidationException("invalid VP token");
	    	
	    }
	    
	    Map<String, Object> claims = signedJWT.getJWTClaimsSet().toJSONObject();
	    
	    if(!claims.containsKey("address") || !claims.containsKey("place_of_birth") || !claims.containsKey("nationality") || !claims.containsKey("given_name") ||!claims.containsKey("family_name") ||
	    		!claims.containsKey("birthdate") ||!claims.containsKey("issuing_country") ||!claims.containsKey("issuing_authority")){
	    	
	    	//return new ResponseEntity<String>("Missing Required Claims", HttpStatus.BAD_REQUEST);
	    	System.out.println("Missing Required Claims");
	    	throw new VpTokenValidationException("Missing Required Claims");
	    	
	    }
	    
	    return new ResponseEntity<String>("Wallet response processed successfully",HttpStatus.OK);
	   
	   
	   
		}catch (Exception e) {
			System.out.println(e.getMessage());
			//return new ResponseEntity<String>("Error Processing response from wallet :"+e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
			throw new VpTokenValidationException("Error Processing response from wallet"+e.getMessage());
		}
	}
	
	
	
	
}
