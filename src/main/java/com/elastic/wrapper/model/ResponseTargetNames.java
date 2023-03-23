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
@JsonPropertyOrder({ "jaro_winkler", "levenshtein","qratio","set_ratio","sort_ratio" })
@Getter
@Setter
@ToString
public class ResponseTargetNames implements Serializable {

	@JsonProperty("jaro_winkler")
	public List<ResponseJaroWinkler> jaroWinkler;
	@JsonProperty("levenshtein")
	public List<ResponseLevenshtein> levenshTein;
	@JsonProperty("qratio")
	public List<ResponseQRatio> qratio;
	@JsonProperty("set_ratio")
	public List<ResponseSetRatio> setRatio;
	@JsonProperty("sort_ratio")
	public List<ResponseSortRatio> sortRatio;
	private final static long serialVersionUID = -7408712712138003342L;

}