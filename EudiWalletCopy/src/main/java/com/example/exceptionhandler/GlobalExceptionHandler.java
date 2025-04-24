package com.example.exceptionhandler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.example.customexception.VpTokenValidationException;
import com.example.helper.ErrorModel;

@ControllerAdvice
public class GlobalExceptionHandler {
	
	
	@ExceptionHandler(VpTokenValidationException.class)
	public ResponseEntity<Object> handleVpTokenValidationException(Exception exception){
		
		ErrorModel errorModel = new ErrorModel("4003", HttpStatus.BAD_REQUEST.value(), exception.getMessage(),"Cannot Validate Token");
		
		return new ResponseEntity<>(errorModel,HttpStatus.BAD_REQUEST);
		
	}

}
