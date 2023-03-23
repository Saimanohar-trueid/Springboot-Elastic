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
@JsonPropertyOrder({ "Source Name","Target Names" })
@Getter
@Setter
@ToString
public class PythonResponseModel implements Serializable {

	@JsonProperty("Source Name")
	public String sourceName;
	@JsonProperty("Target Names")
	public List<ResponseTargetNames> targetNames;
	private final static long serialVersionUID = -1764475197323276074L;

}
