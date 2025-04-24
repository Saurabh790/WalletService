package com.example.controller;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECPoint;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.example.customexception.VpTokenValidationException;
import com.example.helper.PrivatePublicKeys;
import com.example.service.TokenService;
import com.example.service.qrService;
import com.nimbusds.jose.JWEDecrypter;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.ECDHDecrypter;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.crypto.ECDSAVerifier;
import com.nimbusds.jose.shaded.gson.JsonObject;
import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jwt.JWTClaimsSet;
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
	
	
	@GetMapping("/")
	public ModelAndView getAuthorizationRequest() {
		try {
			
			   log.info("inside getAuthorizationRequest");
		
		       String jwtString = tokenService.getSignedJWT();
		       
		       log.info("jwtString : {}" , jwtString);
		       
		       System.out.println(jwtString);
		
			   String clientId = "funke.animo.id";
			   
			   String authRequestUrl = "openid4vp://?client_id=" + clientId +
		                "&request=" + URLEncoder.encode(jwtString, StandardCharsets.UTF_8);
			  
			   byte[]  qrCodeBytes = walletService.generateQRCode(authRequestUrl, 200, 200);
			   String qrCodeBase64 = Base64.getEncoder().encodeToString(qrCodeBytes);
			   
		
		        ModelAndView modelAndView = new ModelAndView("index");
	//	        modelAndView.setViewName("index"); // Set the view name (HTML page)
		        modelAndView.addObject("authRequestUrl", authRequestUrl); // Add the authRequestUrl as a link
		        modelAndView.addObject("qrCodeBase64", qrCodeBase64); // Add the QR code as a Base64 string
		        
		        return modelAndView;
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
			return new ModelAndView();
		}
			   
		
	}
	
	@PostMapping("/walletResponse")
	@ResponseBody
	public ResponseEntity<String> verifyingToken(@RequestParam("response") String response) throws VpTokenValidationException {
		try {
		
		PrivateKey privateKey =privatePublicKeys.loadPrivateKey("private-key-pkcs8.pm");
			
		X509Certificate certificate =privatePublicKeys.loadX509Certificate("xcertificate.pem");
		
		
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
