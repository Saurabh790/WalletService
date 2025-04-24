package com.example.helper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class PrivatePublicKeys {

	private static final Logger log = LoggerFactory.getLogger(PrivatePublicKeys.class);
	
	
//	public static PrivateKey loadPrivateKey(String filePath) throws Exception  {
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
	public static PrivateKey loadPrivateKey(String fileNameInClasspath) throws Exception {
		log.info("fileNameInClasspath :{}",fileNameInClasspath);
		
	    try (InputStream inputStream = PrivatePublicKeys.class.getClassLoader().getResourceAsStream(fileNameInClasspath)) {
	        if (inputStream == null) {
	        	log.error("Private key file not found in classpath");
	            throw new IOException("Private key file not found in classpath: " + fileNameInClasspath);
	            
	           
	            
	        }
	 
	        String keyContent = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
	        String privateKeyPEM = keyContent
	                .replace("-----BEGIN PRIVATE KEY-----", "")
	                .replace("-----END PRIVATE KEY-----", "")
	                .replaceAll("\\s+", "")
	                .trim();
	 
	        byte[] encoded = Base64.getDecoder().decode(privateKeyPEM);
	        KeyFactory keyFactory = KeyFactory.getInstance("EC");
	        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
	        return keyFactory.generatePrivate(keySpec);
	    }
	}
	 
	

	
//	public static X509Certificate loadX509Certificate(String filePath) throws IOException, CertificateException {
//		Path path = Paths.get(filePath);
//		InputStream inputStream  = Files.newInputStream(path);
//	    CertificateFactory certificateFactory =	CertificateFactory.getInstance("X.509");
//	    Certificate certificate = certificateFactory.generateCertificate(inputStream);
//	    X509Certificate x509Certificate = (X509Certificate) certificate;
//	    return x509Certificate;
//		
//		
//	}
	public static X509Certificate loadX509Certificate(String fileNameInClasspath) throws IOException, CertificateException {
		log.info("fileNameInClasspath :{}",fileNameInClasspath);
	    try (InputStream inputStream = PrivatePublicKeys.class.getClassLoader().getResourceAsStream(fileNameInClasspath)) {
	        if (inputStream == null) {
	        	log.error("Certificate file not found in classpath");
	            throw new IOException("Certificate file not found in classpath: " + fileNameInClasspath);
	        }
	 
	        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
	        Certificate certificate = certificateFactory.generateCertificate(inputStream);
	        return (X509Certificate) certificate;
	    }
	}
}
