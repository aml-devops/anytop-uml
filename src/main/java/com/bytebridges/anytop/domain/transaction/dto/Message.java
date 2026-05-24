package com.bytebridges.anytop.domain.transaction.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Message {
	private String code;
	private String reason;
	private String port;
	private String resp;
	@JsonProperty("max-port")
	private String maxPort;

	public Message() {
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

	public String getResp() {
		return resp;
	}

	public void setResp(String resp) {
		this.resp = resp;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getMaxPort() {
		return maxPort;
	}

	public void setMaxPort(String maxPort) {
		this.maxPort = maxPort;
	}

	public Message(String code, String reason, String port, String resp) {
		super();
		this.code = code;
		this.reason = reason;
		this.port = port;
		this.resp = resp;
	}

	@Override
	public String toString() {
		return "Message [code=" + code + ", reason=" + reason + ", port=" + port + ", resp=" + resp + "]";
	}
}
