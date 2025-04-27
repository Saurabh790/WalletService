package com.example.service;

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
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;

import com.example.helper.PrivatePublicKeys;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;

@Component
public class TokenService {
	
	@Autowired
	PrivatePublicKeys privatePublicKeys;
	
//	private static final Resource source1 = new ClassPathResource("private-key-pkcs8.pm");
//	private static final Resource source2 = new ClassPathResource("xcertificate.pem");
	
	Logger log = LoggerFactory.getLogger(TokenService.class);
	
	public String getSignedJWT() throws Exception {
        
//		PrivateKey privateKey =	loadPrivateKey("src/main/resources/private-key-pkcs8.pm");
//		
//		X509Certificate certificate = loadX509Certificate("src/main/resources/xcertificate.pem");
//		
//        PrivateKey privateKey =privatePublicKeys.loadPrivateKey("src/main/resources/private-key-pkcs8.pm");
//		
//		X509Certificate certificate =privatePublicKeys.loadX509Certificate("src/main/resources/xcertificate.pem");
		log.info("inside getSignedJWT() method");
	
		PrivateKey privateKey =privatePublicKeys.loadPrivateKey("private-key-pkcs8.pm");
    	//X509Certificate certificate =privatePublicKeys.loadX509Certificate("x509certificate.pem");
		X509Certificate certificate =privatePublicKeys.loadX509Certificate("x5railcertificate.pem");
		
		PublicKey publicKey = certificate.getPublicKey(); // getting public key from the certificate
		ECPublicKey ecPublicKey = (ECPublicKey) publicKey; // Type Casting the public key to ECPublicKey to access the elliptic curve details.
		ECPoint ecPoint = ecPublicKey.getW(); //ECPoint is a class that represents a point on an elliptic curve.It contains the X and Y coordinates 
		
		BigInteger x = ecPoint.getAffineX(); //X coordinate
		BigInteger y = ecPoint.getAffineY(); // Y coordinate
		
		byte[] xBytes = x.toByteArray();  // Converting X coordinate to byte array
		byte[] yBytes = y.toByteArray(); // Convert Y coordinate to byte array
		
		//walletservice-production.up.railway.app
		
		
		Base64.Encoder urlEncoder = Base64.getUrlEncoder().withoutPadding(); //Get a Base64URL encoder
		String xBase64Url =	urlEncoder.encodeToString(xBytes);//Encode X coordinate
		String yBase64Url = urlEncoder.encodeToString(yBytes);// Encode Y coordinate
		
		System.out.println("x coordinate: "+xBase64Url);
		System.out.println("y coordinate: "+yBase64Url);
		
	 	
				
			JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
					.claim("response_type", "vp_token")
					//.claim("client_id", "eudiwallet-service-alexvicks567-dev.apps.rm3.7wse.p1.openshiftapps.com")
					.claim("client_id", "walletservice-production.up.railway.app")
					.claim("client_id_scheme", "x509_san_dns")
					//.claim("response_uri", "http://eudiwallet-service-alexvicks567-dev.apps.rm3.7wse.p1.openshiftapps.com/walletResponse")
					.claim("response_uri", "https://walletservice-production.up.railway.app/walletResponse")
					.claim("response_mode", "direct_post.jwt")
					.claim("nonce", UUID.randomUUID().toString())
					.claim("state", UUID.randomUUID().toString())
					.claim("client_metadata", Map.of(
							"jwks" , Map.of(
									"keys" , List.of(
											Map.of(
													"use","enc",
													"kty","EC",
													"crv","P256",
													"x", xBase64Url,
													"y", yBase64Url,
													"kid",UUID.randomUUID().toString()
													)
											)
									),
							"authorization_encrypted_response_alg","ECDH-ES",
							"authorization_encrypted_response_enc","A256GCM",
							//"client_id" ,"eudiwallet-service-alexvicks567-dev.apps.rm3.7wse.p1.openshiftapps.com",
							"client_id" ,"walletservice-production.up.railway.app",
							"response_types_supported" , List.of("vp_token")
							)
							)
					.claim("presentation_definition", Map.of("id" , "1ax1k" ,
															 "name" , "Identity Verification" ,
															 "purpose" , "To verify your identity",
															 "input_descriptors" , List.of(
																	 Map.of(
																			 "id" , "xj9qv",
																			 "constraints" , Map.of(
																					 "limit_disclosure" , "required",
																					 "fields" , List.of(
																							 Map.of("path" , List.of("$.given_name")),
																							 Map.of("path", List.of("$.family_name")),
																							 Map.of("path", List.of("$.birthdate")),
																							 Map.of("path", List.of("$.place_of_birth")),
																							 Map.of("path", List.of("$.nationality")),
																							 Map.of("path", List.of("$.address")),
																							 Map.of("path", List.of("$.issuing_country")),
																							 Map.of("path", List.of("$.issuing_authority"))
																									 )
																							 
																							 
																							 )
																					 
																					 )
																			 
																			 )
																	 )
							)
					//.issuer("http://eudiwallet-service-alexvicks567-dev.apps.rm3.7wse.p1.openshiftapps.com/walletResponse")
					.issuer("https://walletservice-production.up.railway.app/walletResponse")
					.audience("https://self-issued.me/v2")
					.expirationTime(new Date(System.currentTimeMillis()+60000))
					.notBeforeTime(new Date())
					.issueTime(new Date())
					.jwtID(UUID.randomUUID().toString())
					.build();
			
			
			    byte[] encodedCertificate =  certificate.getEncoded(); // Getting the DER-encoded certificate(converting certificate data into binary format because it stores as a copm plex object)
			    String base64EncodedCertificate  =  Base64.getEncoder().encodeToString(encodedCertificate); //Base64-encode the certificate (converting in  the format of A-Z, a-z, 0-9, +, /) and adds padding (=) )
			    Base64URL base64URLCertiifcate = new Base64URL(base64EncodedCertificate); //Wrap the Base64-encoded certificate in a Base64URL object
			    List<com.nimbusds.jose.util.Base64>  x509CertChain  =  Collections.singletonList(base64URLCertiifcate); // creating a list containing the Base64URL certificate (x5c claim)
			    JWSHeader.Builder headerBuilder = new JWSHeader.Builder(JWSAlgorithm.ES256); //Creating a JWSHeader.Builder with the ES256 algorithm
			    headerBuilder.x509CertChain(x509CertChain); //Adding the x5c claim to the header
			    JWSHeader jwsHeader = headerBuilder.build(); //Building the JWSHeader
			    
			   SignedJWT signedJWT = new SignedJWT(jwsHeader, claimsSet); //Creating the SignedJWT object with the header and claims set
			   
			   ECDSASigner signer = new ECDSASigner((ECPrivateKey) privateKey);   // Signing the JWT using the private key
			   signedJWT.sign(signer);
			   
			   String jwtString = signedJWT.serialize(); // Serializing the JWT
			   
			   return jwtString;
			   
			   
	}
//	private static PrivateKey loadPrivateKey(String filePath) throws Exception  {
//		Path path = Paths.get(filePath);
//	    byte[] keyBytes =	Files.readAllBytes(path);
//	    String keyContent = new String(keyBytes, StandardCharsets.UTF_8); 
//	    String  privateKeyPEM = keyContent.replace("-----BEGIN PRIVATE KEY-----", "").replace("-----END PRIVATE KEY-----", "").replaceAll("\\s+", "").trim();
//	    byte[] encoded =Base64.getDecoder().decode(privateKeyPEM);
//	    KeyFactory keyFactory =  KeyFactory.getInstance("EC");
//	    PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
//	    return keyFactory.generatePrivate(keySpec);
//	    
//		
//	}
//	
//	private static X509Certificate loadX509Certificate(String filePath) throws IOException, CertificateException {
//		Path path = Paths.get(filePath);
//		InputStream inputStream  = Files.newInputStream(path);
//	    CertificateFactory certificateFactory =	CertificateFactory.getInstance("X.509");
//	    Certificate certificate = certificateFactory.generateCertificate(inputStream);
//	    X509Certificate x509Certificate = (X509Certificate) certificate;
//	    return x509Certificate;
//		
//		
//	}


}
