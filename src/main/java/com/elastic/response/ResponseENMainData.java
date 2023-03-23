package com.elastic.response;

import java.util.List;

import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResponseENMainData {
	
	
	private ResponseENData data;
	
	private List<Aliases> aliases;
	
	public ResponseENMainData() {
		// TODO Auto-generated constructor stub
	}

}
