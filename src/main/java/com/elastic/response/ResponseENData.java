package com.elastic.response;

import java.util.List;

import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResponseENData {
	
	
	private String uid;

	private String firstname;

	private String lastname;

	private String fullname;

	//private List<String> aliases;
	
	private String entityType;
	
	public ResponseENData() {
		// TODO Auto-generated constructor stub
	}
	
	

}
