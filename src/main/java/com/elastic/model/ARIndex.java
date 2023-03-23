package com.elastic.model;

import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

public class ARIndex {
	
	@Field(type = FieldType.Text, name = "primary_name")
	private String primary_name;
	
	@Field(type = FieldType.Object, name = "data")
	private ElasticARData data;
	
	public ARIndex() {
		// TODO Auto-generated constructor stub
	}

	public ARIndex(String primary_name, ElasticARData data) {
		//super();
		this.primary_name = primary_name;
		this.data = data;
	}

	public String getPrimary_name() {
		return primary_name;
	}

	public void setPrimary_name(String primary_name) {
		this.primary_name = primary_name;
	}

	public ElasticARData getData() {
		return data;
	}

	public void setData(ElasticARData data) {
		this.data = data;
	}
	
	

}
