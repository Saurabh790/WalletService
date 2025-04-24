package com.example.helper;


import lombok.ToString;

@ToString
public class ErrorModel {

	private String code;

	private String reason;

	private String message;

	private Integer status;
	
	public ErrorModel() {
		
	}

	public ErrorModel(String code, Integer status, String message, String reason) {
		this.code = code;
		this.status = status;
		this.message = message;
		this.reason = reason;	
	}


	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

}
