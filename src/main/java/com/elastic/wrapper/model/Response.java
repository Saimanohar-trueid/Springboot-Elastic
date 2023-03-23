package com.elastic.wrapper.model;

import java.io.Serializable;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "STATUSINFO", "WORLDCHECKMATCHRESPONSE" })
@Getter
@Setter
@ToString
public class Response implements Serializable {

	@JsonProperty("STATUSINFO")
	public StatusInfo statusInfo;
	@JsonProperty("WORLDCHECKMATCHRESPONSE")
	public WorldCheckMatchResponse worldcheckmatchresponse;
	private final static long serialVersionUID = -7408712712138003342L;

}